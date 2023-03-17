/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import io.github.risu729.erutcurts.BotInfo;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class EmbedUtil {

  @Contract(pure = true)
  public @NotNull EmbedBuilder createDefaultBuilder(@NotNull String title) {
    return new EmbedBuilder().setTitle(title).setColor(BotInfo.THEME_COLOR);
  }
}
