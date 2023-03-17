/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.misc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.MoreCollectors;
import com.google.gson.reflect.TypeToken;
import io.github.risu729.erutcurts.DiscordDB;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.CommandOptionUtil;
import io.github.risu729.erutcurts.util.EmbedUtil;
import io.github.risu729.erutcurts.util.interaction.ExecutableSlashCommandData;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.With;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Settings extends ExecutableSlashCommandData {

  private static final String COMMAND_NAME = "settings";
  private static final String COMMAND_LIST = "list";
  private static final String COMMAND_SET = "set";
  private static final String COMMAND_AUTOGENERATE = "autogenerate";
  // private static final String COMMAND_AUTO_TRANSLATE = "auto-translate";
  private static final String COMMAND_VALUE = "value";

  private static final String DATABASE_NAME = "Settings";

  private static final Settings INSTANCE = new Settings(); // singleton

  private Settings() {
    super(
        Commands.slash(COMMAND_NAME, "設定を管理します")
            .addSubcommands(new SubcommandData(COMMAND_LIST, "設定を表示します"))
            .addSubcommandGroups(
                new SubcommandGroupData(COMMAND_SET, "設定を変更します")
                    .addSubcommands(
                        new SubcommandData(COMMAND_AUTOGENERATE, "ストラクチャーの自動変換を変更します")
                            .addOptions(
                                CommandOptionUtil.createWithBlankDescription(
                                        OptionType.BOOLEAN, COMMAND_VALUE)
                                    .setRequired(true)))
                /*.addSubcommands(
                new SubcommandData(COMMAND_AUTO_TRANSLATE, "更新履歴(changelog)の自動翻訳を変更します")
                    .addOptions(
                        CommandOptionUtil.createWithBlankDescription(
                                OptionType.BOOLEAN, COMMAND_VALUE)
                            .setRequired(true)))*/ )
            .setGuildOnly(true));
  }

  public static @NotNull Settings getInstance() {
    return INSTANCE;
  }

  @Override
  public void execute(@NotNull SlashCommandInteractionEvent event) {
    checkState(event.isFromGuild()); // this command is guild only

    var guildId = checkNotNull(event.getGuild()).getId();

    switch (event.getFullCommandName().split(" ")[1]) {
      case COMMAND_LIST -> event.deferReply(true).queue();
      case COMMAND_SET -> {
        event.deferReply().queue();
        set(event, guildId);
      }
      default -> throw new IllegalArgumentException(
          "Unknown command path: " + event.getFullCommandName());
    }

    sendList(event.getHook(), guildId);
  }

  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  private void set(@NotNull SlashCommandInteractionEvent event, @NotNull String guildId) {
    // options are required, so we can assume that they are present
    UnaryOperator<SettingsData> updater =
        oldData ->
            switch (checkNotNull(event.getSubcommandName())) {
              case COMMAND_AUTOGENERATE -> oldData.withPackAutoGeneration(
                  checkNotNull(event.getOption(COMMAND_VALUE, OptionMapping::getAsBoolean)));
                /*case COMMAND_AUTO_TRANSLATE -> oldData.withChangelogAutoTranslation(
                checkNotNull(event.getOption(COMMAND_VALUE, OptionMapping::getAsBoolean)));*/
              default -> throw new IllegalArgumentException(
                  "Unknown subcommand: " + event.getSubcommandName());
            };
    updateSettingsData(guildId, updater);
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private void sendList(@NotNull InteractionHook hook, @NotNull String guildId) {
    hook.sendMessageEmbeds(
            EmbedUtil.createDefaultBuilder("設定")
                .addField(
                    "ストラクチャーファイル自動変換", isPackAutoGenerationEnabled(guildId) ? "有効" : "無効", false)
                /*.addField(
                "更新履歴自動翻訳", isChangelogAutoTranslationEnabled(guildId) ? "有効" : "無効", false)*/
                .build())
        .queue();
  }

  @Contract(pure = true)
  public boolean isPackAutoGenerationEnabled(@NotNull String guildId) {
    return getSettingsData(guildId)
        .map(SettingsData::packAutoGeneration)
        .orElse(SettingsData.DEFAULT_PACK_AUTO_GENERATION);
  }

  /*@Contract(pure = true)
  public boolean isChangelogAutoTranslationEnabled(@NotNull String guildId) {
    return getSettingsData(guildId)
        .map(SettingsData::changelogAutoTranslation)
        .orElse(SettingsData.DEFAULT_CHANGELOG_AUTO_TRANSLATION);
  }*/

  // contains only non-default settings
  @SuppressWarnings("EmptyClass")
  @CheckReturnValue
  private @NotNull SortedSet<@NotNull SettingsData> getSettings() {
    return DiscordDB.get(
        DATABASE_NAME,
        new TypeToken<>() {},
        set ->
            set.stream()
                .filter(Predicate.not(SettingsData::isDefault)) // remove default settings
                .filter(
                    data ->
                        Erutcurts.getJDA().getGuilds().stream()
                            .map(Guild::getId)
                            .anyMatch(data.guildId()::equals)) // remove guilds bot has left
                .collect(Collectors.toCollection(TreeSet::new)),
        TreeSet::new);
  }

  @Contract(pure = true)
  private @NotNull Optional<@NotNull SettingsData> getSettingsData(@NotNull String guildId) {
    return getSettings().stream()
        .filter(settings -> settings.guildId().equals(guildId))
        .collect(MoreCollectors.toOptional());
  }

  private void updateSettingsData(
      @NotNull String guildId, @NotNull UnaryOperator<SettingsData> updater) {
    var settings = getSettings();
    var oldData = getSettingsData(guildId).orElseGet(() -> new SettingsData(guildId));
    settings.remove(oldData);
    var newData = updater.apply(oldData);
    if (!newData.isDefault()) {
      settings.add(newData);
    }
  }

  // all new fields must be nullable or primitive for the backward compatibility
  // Gson assigns null or the default value to the components that are not present in the JSON
  @With
  private record SettingsData(
      @NotNull String guildId, boolean packAutoGeneration /*, boolean changelogAutoTranslation*/)
      implements Comparable<SettingsData> {

    private static final boolean DEFAULT_PACK_AUTO_GENERATION = true;
    // private static final boolean DEFAULT_CHANGELOG_AUTO_TRANSLATION = true;

    private static final Comparator<SettingsData> COMPARATOR =
        Comparator.comparing(SettingsData::guildId);

    private SettingsData(@NotNull String guildId) {
      this(guildId, DEFAULT_PACK_AUTO_GENERATION /*, DEFAULT_CHANGELOG_AUTO_TRANSLATION*/);
    }

    /*private @NotNull SettingsData withChangelogAutoTranslation(boolean changelogAutoTranslation) {
      if (this.changelogAutoTranslation == changelogAutoTranslation) {
        return this;
      }
      return new SettingsData(guildId, packAutoGeneration, changelogAutoTranslation);
    }*/

    @Contract(pure = true)
    private boolean isDefault() {
      return packAutoGeneration == DEFAULT_PACK_AUTO_GENERATION;
      // && changelogAutoTranslation == DEFAULT_CHANGELOG_AUTO_TRANSLATION;
    }

    @Override
    public int compareTo(@NotNull SettingsData other) {
      return COMPARATOR.compare(this, other);
    }
  }
}
