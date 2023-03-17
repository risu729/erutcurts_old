/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public abstract class ExecutableSlashCommandData
    implements SlashCommandData, Executable<SlashCommandInteractionEvent> {

  private static final boolean DEFAULT_ADMIN_GUILD_ONLY = false;

  @Delegate @NotNull SlashCommandData slashCommandData;
  boolean isAdminGuildOnly;

  protected ExecutableSlashCommandData(@NotNull SlashCommandData slashCommandData) {
    this(slashCommandData, DEFAULT_ADMIN_GUILD_ONLY);
  }

  @Override
  public final @NotNull String getKey() {
    return slashCommandData.getName();
  }
}
