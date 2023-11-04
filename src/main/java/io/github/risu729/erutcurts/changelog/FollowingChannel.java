/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.changelog;

import java.util.Optional;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

record FollowingChannel(@NotNull String serverName, @NotNull String channelName) {

  private static final String CHANNEL_PREFIX = "#";

  @Contract("_ -> new")
  static @NotNull Optional<FollowingChannel> fromMessageAuthor(@NotNull User user) {
    // e.g. "MINECRAFT #java-changelogs"
    var name = user.getName();
    var channelPrefixIndex = name.lastIndexOf(CHANNEL_PREFIX); // cannot include # in channel name
    if (channelPrefixIndex == -1) {
      return Optional.empty();
    }
    // -1 to remove trailing space of server name
    return Optional.of(
        new FollowingChannel(
            name.substring(0, channelPrefixIndex - 1), name.substring(channelPrefixIndex + 1)));
  }

  public @NotNull String toString() {
    return serverName + ' ' + CHANNEL_PREFIX + channelName;
  }
}
