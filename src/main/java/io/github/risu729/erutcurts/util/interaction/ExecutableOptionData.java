/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class ExecutableOptionData extends OptionData
    implements Executable<CommandAutoCompleteInteractionEvent>, Cloneable {

  protected ExecutableOptionData(
      @NotNull OptionType type, @NotNull String name, @NotNull String description) {
    super(type, name, description, false, true);
  }

  protected ExecutableOptionData(
      @NotNull OptionType type,
      @NotNull String name,
      @NotNull String description,
      boolean isRequired) {
    super(type, name, description, isRequired, true);
  }

  @Override
  public final @NotNull ExecutableOptionData clone() {
    try {
      return (ExecutableOptionData) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public final @NotNull String getKey() {
    return getName();
  }
}
