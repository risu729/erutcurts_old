/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.changelog;

import io.github.risu729.erutcurts.Envs;
import io.github.risu729.erutcurts.util.interaction.ListenerWithRegistry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

// TODO: support changelog webpage translation
public final class ChangelogListener extends ListenerWithRegistry {

  private static final String MINECRAFT_SERVER_NAME = "MINECRAFT";
  private static final FollowingChannel MCJE_CHANGELOGS =
      new FollowingChannel(MINECRAFT_SERVER_NAME, "java-changelogs");
  private static final FollowingChannel MCBE_CHANGELOGS =
      new FollowingChannel(MINECRAFT_SERVER_NAME, "bedrock-changelogs");

  private static final Pattern VERSION_PATTERN =
      Pattern.compile("\\d+\\.\\d+\\.\\d+(?:\\.\\d+)?|\\d+w\\d+a");

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {

    var message = event.getMessage();

    // ignore message not from #bedrock-changelogs or #java-changelogs
    if (!message.getFlags().contains(Message.MessageFlag.IS_CROSSPOST)) {
      return;
    }
    var followingChannel =
        FollowingChannel.fromMessageAuthor(message.getAuthor())
            .filter(
                following ->
                    following.equals(MCJE_CHANGELOGS) || following.equals(MCBE_CHANGELOGS));
    if (followingChannel.isEmpty()) {
      return;
    }

    var firstLine = message.getContentRaw().lines().findFirst().orElseThrow();

    // if the message is in the changelogs channel, create a thread storing translated messages
    if (event.getChannel().getId().equals(Envs.getEnv("CHANGELOGS_CHANNEL_ID"))) {
      if (message.getStartedThread() == null) {
        message.createThreadChannel(firstLine).queue();
      }
    }

    event
        .getChannel()
        .sendMessage(
            DeepLTranslator.translateMessage(
                message, DiscordLocale.ENGLISH_US, event.getGuild().getLocale()))
        .queue();

    // ignore url in the message without a version in the first line
    if (!message
        .getContentRaw()
        .lines()
        .findFirst()
        .map(VERSION_PATTERN::matcher)
        .map(Matcher::find)
        .orElse(false)) {}
  }
}
