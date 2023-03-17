/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

public interface Executable<E extends GenericInteractionCreateEvent> {

  void execute(@NotNull E event);

  @NotNull
  String getKey();
}
