/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.MoreCollectors;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.Attachments;
import io.github.risu729.erutcurts.util.Caches;
import io.github.risu729.erutcurts.util.EmbedUtil;
import io.github.risu729.erutcurts.util.interaction.ExecutableSlashCommandData;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
public final class PackageMode extends ExecutableSlashCommandData {

  private static final String COMMAND_NAME = "package";

  private static final int MESSAGE_HISTORY_LIMIT = 50;
  private static final PackageMode INSTANCE = new PackageMode();

  LoadingCache<String, Boolean> enabledChannelCache =
      Caches.newLoadingCache(channelId -> getStartMessage(channelId).isPresent());

  private PackageMode() {
    super(
        Commands.slash(COMMAND_NAME, "パッケージを使用します")
            .addSubcommands(
                Arrays.stream(Subcommand.values()).map(Subcommand::subcommandData).toList()));
  }

  static @NotNull PackageMode getInstance() {
    return INSTANCE;
  }

  @Contract(pure = true)
  boolean isPackageModeEnabled(@NotNull String channelId) {
    return enabledChannelCache.get(channelId);
  }

  @Override
  public void execute(@NotNull SlashCommandInteractionEvent event) {

    event.deferReply().queue();

    var subcommand =
        Subcommand.fromSubcommandName(checkNotNull(event.getSubcommandName())).orElseThrow();
    Boolean isPackageModeAfter = subcommand.packageModeAfterExec();
    var channelId = checkNotNull(event.getChannel()).getId();
    boolean isPackageMode = isPackageModeEnabled(channelId);

    if (isPackageModeAfter != null && !Objects.equals(isPackageModeAfter, isPackageMode)) {
      enabledChannelCache.put(channelId, isPackageModeAfter);
    }

    if (isPackageMode && subcommand == Subcommand.CONVERT) {
      var attachments =
          Erutcurts.getMessageChannelById(channelId)
              .getHistoryAfter(getStartMessage(channelId).orElseThrow(), MESSAGE_HISTORY_LIMIT)
              .complete()
              .getRetrievedHistory()
              .stream()
              .filter(Predicate.not(Erutcurts::isSelfMessage))
              .map(
                  message ->
                      Attachments.getAttachmentsWithExtension(
                          message, MCExtension.MCSTRUCTURE.toString()))
              .flatMap(List::stream)
              .toList();
      event.getHook().sendFiles(TargetType.fromEvent(event).convert(attachments)).queue();
      return;
    }

    String description;
    // use Objects::equals to compare boolean and Nullable Boolean
    if (Objects.equals(isPackageMode, isPackageModeAfter)) {
      description = isPackageModeAfter ? "既に開始されています" : "開始されていません";
    } else {
      description =
          switch (subcommand) {
            case START -> "開始しました";
            case CANCEL -> "キャンセルしました";
            case CONVERT -> "パッケージが開始されていません";
            case STATUS -> isPackageMode ? "開始されています" : "開始されていません";
          };
    }

    event
        .getHook()
        .sendMessageEmbeds(
            EmbedUtil.createDefaultBuilder("パッケージ").setDescription(description).build())
        .queue();
  }

  // check if the last executed package command is start
  @Contract(pure = true)
  private @NotNull Optional<Message> getStartMessage(@NotNull String channelId) {
    return Erutcurts.getMessageChannelById(channelId).getIterableHistory().stream()
        .limit(MESSAGE_HISTORY_LIMIT)
        .filter(Erutcurts::isSelfMessage)
        .filter(message -> Subcommand.fromMessage(message).isPresent())
        .findFirst()
        .filter(message -> Subcommand.fromMessage(message).orElseThrow() == Subcommand.START);
  }

  @Getter
  @Accessors(fluent = true)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  private enum Subcommand {
    START(true, "パッケージを開始します"),
    CANCEL(false, "パッケージをキャンセルします"),
    CONVERT(false, "パッケージに含まれるファイルをまとめて変換します", TargetType.OPTION),
    STATUS("パッケージの状態を確認します");

    @Nullable Boolean packageModeAfterExec;
    @NotNull SubcommandData subcommandData;

    Subcommand(@NotNull String description) {
      this(null, description);
    }

    Subcommand(
        @Nullable Boolean packageModeAfterExec,
        @NotNull String description,
        @NotNull OptionData @NotNull ... options) {
      this.packageModeAfterExec = packageModeAfterExec;
      this.subcommandData =
          new SubcommandData(name().toLowerCase(Locale.ENGLISH), description).addOptions(options);
    }

    @Contract(pure = true)
    private static @NotNull Optional<Subcommand> fromSubcommandName(@NotNull String subcommand) {
      return Arrays.stream(values())
          .filter(element -> element.name().equalsIgnoreCase(subcommand))
          .collect(MoreCollectors.toOptional());
    }

    @Contract(pure = true)
    private static @NotNull Optional<Subcommand> fromMessage(@NotNull Message message) {
      return Optional.ofNullable(message.getInteraction())
          .map(Message.Interaction::getName)
          .map(interaction -> interaction.split(" "))
          .filter(interaction -> interaction[0].equalsIgnoreCase(COMMAND_NAME))
          .map(interaction -> interaction[1])
          .flatMap(Subcommand::fromSubcommandName);
    }
  }
}
