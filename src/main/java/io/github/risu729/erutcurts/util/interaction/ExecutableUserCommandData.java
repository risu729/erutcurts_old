/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public abstract class ExecutableUserCommandData
    implements CommandData, Executable<UserContextInteractionEvent> {

  @Delegate @NotNull CommandData commandData;

  protected ExecutableUserCommandData(@NotNull CommandData commandData) {
    checkArgument(commandData.getType() == Command.Type.USER);
    this.commandData = commandData;
  }

  @Override
  public final @NotNull String getKey() {
    return commandData.getName();
  }
}
