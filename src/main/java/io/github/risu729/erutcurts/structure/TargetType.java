/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure;

import com.google.common.collect.MoreCollectors;
import io.github.risu729.erutcurts.structure.behavior.Behavior;
import io.github.risu729.erutcurts.structure.behavior.Identifier;
import io.github.risu729.erutcurts.structure.behavior.World;
import io.github.risu729.erutcurts.util.Attachments;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
enum TargetType {
  BEHAVIOR(true),
  SINGLE_BEHAVIOR(false),
  WORLD(true);
  /*
  STRUCTURA(false),
  SLICED_IMAGES(false),
  */

  private static final String COMMAND_TYPE = "type";
  static final OptionData OPTION =
      new OptionData(OptionType.STRING, COMMAND_TYPE, "変換先")
          .setRequired(true)
          .addChoices(
              Arrays.stream(values())
                  .map(TargetType::toString)
                  .map(name -> new Command.Choice(name, name))
                  .toList());

  @NotNull String name = name().toLowerCase(Locale.ENGLISH).replace('_', '-');

  @Getter boolean isMultipleFiles;

  @Contract(pure = true)
  static @NotNull TargetType fromEvent(@NotNull SlashCommandInteractionEvent event) {
    var optionStr = event.getOption(COMMAND_TYPE, OptionMapping::getAsString);
    return Arrays.stream(values())
        .filter(type -> type.toString().equals(optionStr))
        .collect(MoreCollectors.onlyElement());
  }

  @CheckReturnValue
  @NotNull
  List<@NotNull FileUpload> convert(
      @NotNull Collection<? extends Message.@NotNull Attachment> attachments) {
    try (var closeableTempDir = CloseablePath.of(FileUtil.createTempDir())) {
      var tempDir = closeableTempDir.path();
      Map<Identifier, Path> attachmentPaths =
          attachments.stream()
              .collect(
                  Collectors.toUnmodifiableMap(
                      attachment ->
                          Identifier.fromString(
                              FileUtil.getFilenameWithoutExtension(attachment.getFileName())),
                      attachment -> Attachments.download(attachment, tempDir, true)));

      if (isMultipleFiles) {
        switch (this) {
          case BEHAVIOR -> {
            try (var closeablePath = Behavior.generate(attachmentPaths)) {
              return List.of(FileUpload.fromData(closeablePath.path()));
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }
          case WORLD -> {
            try (var closeablePath = World.generate(attachmentPaths)) {
              return List.of(FileUpload.fromData(closeablePath.path()));
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }
          default -> throw new AssertionError();
        }
      }

      // target types converts into multiple files
      return attachmentPaths.entrySet().stream()
          .map(
              entry -> {
                var structureName = entry.getKey();
                var structurePath = entry.getValue();
                if (this == SINGLE_BEHAVIOR) {
                  try (var closeablePath =
                      Behavior.generate(Map.of(structureName, structurePath))) {
                    return FileUpload.fromData(closeablePath.path());
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                  /*case STRUCTURA -> throw new UnsupportedOperationException(
                      "Structura is not supported yet");
                  case SLICED_IMAGES -> throw new UnsupportedOperationException(
                      "Sliced images are not supported yet");*/
                }
                throw new AssertionError();
              })
          .toList();
    }
  }

  @Override
  public @NotNull String toString() {
    return name;
  }
}
