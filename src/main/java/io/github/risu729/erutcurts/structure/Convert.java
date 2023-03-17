/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure;

import io.github.risu729.erutcurts.util.CommandOptionUtil;
import io.github.risu729.erutcurts.util.interaction.ExecutableSlashCommandData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

final class Convert extends ExecutableSlashCommandData {

  private static final String COMMAND_FILE = "file";
  private static final Convert INSTANCE = new Convert();

  private Convert() {
    super(
        Commands.slash("convert", "ストラクチャーファイルを変換します")
            .addOptions(TargetType.OPTION)
            .addOptions(
                CommandOptionUtil.createVarArgs(
                    new OptionData(OptionType.ATTACHMENT, COMMAND_FILE, "変換する.mcstructureファイル")
                        .setRequired(true),
                    1,
                    OptionData.MAX_CHOICES - 2))); // -2 for type option and required one
  }

  static @NotNull Convert getInstance() {
    return INSTANCE;
  }

  @Override
  public void execute(@NotNull SlashCommandInteractionEvent event) {
    event.deferReply().queue(); // defer reply to prevent timeout
    var attachments =
        event.getOptionsByType(OptionType.ATTACHMENT).stream()
            .filter(option -> option.getName().startsWith(COMMAND_FILE))
            .map(OptionMapping::getAsAttachment)
            .toList();
    event.getHook().sendFiles(TargetType.fromEvent(event).convert(attachments)).queue();
  }
}
