/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.misc;

import io.github.risu729.erutcurts.BotInfo;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.interaction.ListenerWithRegistry;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public final class MiscListener extends ListenerWithRegistry {

  public MiscListener() {
    super(Help.getInstance(), Settings.getInstance());
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    Notifications.sendNotification(
        "%s is Now Ready!%nJoining Guilds: %s"
            .formatted(
                BotInfo.NAME,
                Erutcurts.getJDA().getGuilds().stream().map(Guild::getName).toList()));
  }

  @Override
  public void onGuildJoin(@NotNull GuildJoinEvent event) {
    // don't show member count because it needs GUILD_MEMBERS, a privileged intent
    event
        .getGuild()
        .retrieveOwner()
        .map(Member::getUser)
        .map(User::getName)
        .map(owner -> "Joined Guild: %s, Owner: %s".formatted(event.getGuild().getName(), owner))
        .queue(Notifications::sendNotification);
  }

  @Override
  public void onGuildLeave(@NotNull GuildLeaveEvent event) {
    Notifications.sendNotification("Left Guild: %s".formatted(event.getGuild().getName()));
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    var message = event.getMessage();
    Optional.ofNullable(message.getReferencedMessage())
        .filter(Erutcurts::isSelfMessage)
        .map(Message::getEmbeds)
        .filter(embeds -> embeds.size() == 1)
        .map(list -> list.get(0))
        .flatMap(DataRequest::fromEmbed)
        .ifPresent(type -> type.process(message.getAttachments()));
  }

  @Override
  public void shutdown() {
    Notifications.sendNotification("%s is now shutting down...".formatted(BotInfo.NAME));
  }
}
