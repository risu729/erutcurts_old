/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.misc;

import io.github.risu729.erutcurts.BotInfo;
import io.github.risu729.erutcurts.Envs;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.EmbedUtil;
import io.github.risu729.erutcurts.util.interaction.ExecutableSlashCommandData;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

final class Help extends ExecutableSlashCommandData {

  private static final LocalDate LAST_EDIT = LocalDate.of(2023, 3, 10);

  private static final String COMMAND_NAME = "help";
  private static final String COMMAND_INFO_NAME = "info";

  private static final Help INSTANCE = new Help(); // singleton

  @SuppressWarnings("ClassExtendsConcreteCollection")
  private static final List<MessageEmbed.Field> INFO_FIELDS =
      new LinkedHashMap<String, String>() {
        {
          put("Name", BotInfo.NAME);
          put("Version", BotInfo.VERSION);
          put("Developer", BotInfo.DEVELOPER);
          put(
              "Supported Languages",
              BotInfo.SUPPORTED_LOCALES.stream()
                  .map(DiscordLocale::getLanguageName)
                  .collect(Collectors.joining(", ")));
          put("Java Version", System.getProperty("java.version"));
          put("Java Vendor", System.getProperty("java.vendor"));
          put("Server OS", System.getProperty("os.name") + System.getProperty("os.version"));
          put("JDA Version", JDAInfo.VERSION);
          put("Bedrock Samples Version", BotInfo.BEDROCK_SAMPLES_VERSION);
          put(
              "Start Time",
              Erutcurts.START_TIME
                  .truncatedTo(ChronoUnit.MINUTES)
                  .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
      }.entrySet().stream()
          .map(entry -> new MessageEmbed.Field(entry.getKey(), entry.getValue(), true))
          .toList();

  private Help() {
    super(
        Commands.slash(COMMAND_NAME, "ヘルプを表示します")
            .addOptions(new OptionData(OptionType.BOOLEAN, COMMAND_INFO_NAME, "詳細情報を表示するか")));
  }

  static @NotNull Help getInstance() {
    return INSTANCE;
  }

  @Override
  public void execute(@NotNull SlashCommandInteractionEvent event) {
    var newAction =
        event
            .replyEmbeds(
                EmbedUtil.createDefaultBuilder("ヘルプ")
                    .setDescription(
                        """
                        このBotは、Minecraft Bedrock Edition の技術勢に向けたBotです。

                        **機能一覧**
                        ・ ストラクチャーファイル(.mcstructure)をビヘイビアパック・ワールドへ自動で変換します。

                        **コマンド一覧**
                        ・ /help このヘルプを表示します。
                        ・ /convert ストラクチャーファイルを変換します。
                        ・ /package 自動変換を一時停止し、/package convert で送信したファイルをまとめて変換します。
                        ・ /settings 設定を確認・変更します。

                        **iOSへの対応について**
                        iOSでは、Discordアプリからファイル送信ができないため、ファイルアプリなどから"共有"でDiscordへ送信してください。
                        /convert コマンドでファイルを添付することもできないため、複数ファイルをまとめて変換したいときや、ワールドへ変換したいときは、/package コマンドを使用して上記の方法で送信してください。

                        **サポート**
                        何か質問があれば、%s までDMでお問い合わせください。
                        また、GitHubのIssueへの投稿も歓迎します。
                        """
                            .formatted(
                                Erutcurts.getJDA()
                                    .retrieveUserById(Envs.getEnv("ADMIN_USER_ID"))
                                    .complete()
                                    .getAsTag()))
                    .setTimestamp(OffsetDateTime.of(LAST_EDIT.atStartOfDay(), ZoneOffset.UTC))
                    .build())
            .addComponents(ActionRow.partitionOf(Button.link(BotInfo.GITHUB_URL, "GitHub")))
            .setEphemeral(true);
    if (event.getOption(COMMAND_INFO_NAME, false, OptionMapping::getAsBoolean)) {
      var builder = EmbedUtil.createDefaultBuilder("情報");
      INFO_FIELDS.forEach(builder::addField);
      builder.addField(
          "Ping",
          (-event.getTimeCreated().toInstant().toEpochMilli() + System.currentTimeMillis()) + " ms",
          true);
      newAction.addEmbeds(builder.build());
    }
    newAction.queue();
  }
}
