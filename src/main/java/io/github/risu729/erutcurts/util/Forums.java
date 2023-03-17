/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import com.google.common.collect.MoreCollectors;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Forums {

  private final String DUMMY_MESSAGE = "\u200B"; // Zero-width space

  public @NotNull ThreadChannel createEmptyForumPost(
      @NotNull ForumChannel channel, @NotNull String name) {
    var post =
        channel.createForumPost(name, MessageCreateData.fromContent(DUMMY_MESSAGE)).complete();
    post.getMessage().delete().queue();
    return post.getThreadChannel();
  }

  @Contract(pure = true)
  public @NotNull Optional<ThreadChannel> getForumPost(
      @NotNull IThreadContainer channel, @NotNull String name) {
    return channel.getThreadChannels().stream()
        .filter(thread -> thread.getName().equals(name))
        .collect(MoreCollectors.toOptional());
  }
}
