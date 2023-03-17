/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.misc;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.MoreCollectors;
import io.github.risu729.erutcurts.structure.behavior.LevelVersions;
import io.github.risu729.erutcurts.structure.behavior.World;
import io.github.risu729.erutcurts.util.Attachments;
import io.github.risu729.erutcurts.util.EmbedUtil;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.lingala.zip4j.ZipFile;
import nl.itslars.mcpenbt.NBTUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DataRequest {
  EXPORTED_FLAT_WORLD(
      """
                LevelVersions data could not be found.
                Please create and export a new flat world and reply to this message.
                Leave the flat world settings as default except for the "Flat World" toggle.""") {
    @Override
    public void process(List<? extends Message.@NotNull Attachment> attachments) {
      checkArgument(attachments.size() == 1);
      try (var closeableTempDir = CloseablePath.of(FileUtil.createTempDir())) {
        var tempDir = closeableTempDir.path();
        var compressedWorld = Attachments.download(attachments.get(0), tempDir, false);
        try (var zipFile = new ZipFile(compressedWorld.toFile())) {
          zipFile.extractFile(World.LEVEL_FILENAME.toString(), tempDir.toString());
        }
        var level = tempDir.resolve(World.LEVEL_FILENAME);
        LevelVersions.update(NBTUtil.read(true, level).getAsCompound());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  };

  private static final String DATA_REQUEST_TITLE = "Data Request";

  @NotNull String description;

  DataRequest(@NotNull String description) {
    this.description = description;
  }

  @Contract(pure = true)
  static @NotNull Optional<DataRequest> fromEmbed(@NotNull MessageEmbed embed) {
    if (!DATA_REQUEST_TITLE.equals(embed.getTitle())) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(type -> type.description().equals(embed.getDescription()))
        .collect(MoreCollectors.toOptional());
  }

  @Contract(pure = true)
  @NotNull
  MessageEmbed createEmbed() {
    return EmbedUtil.createDefaultBuilder(DATA_REQUEST_TITLE)
        .setColor(Color.ORANGE)
        .setDescription(description)
        .build();
  }

  public abstract void process(List<? extends Message.@NotNull Attachment> attachments);
}
