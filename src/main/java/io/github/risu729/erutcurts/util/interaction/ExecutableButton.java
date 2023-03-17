/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public abstract class ExecutableButton implements Button, Executable<ButtonInteractionEvent> {

  @Delegate @NotNull Button button;

  protected ExecutableButton(@NotNull Button button) {
    checkArgument(button.getStyle() != ButtonStyle.LINK);
    this.button = button;
  }

  @Override
  public final @NotNull String getKey() {
    return checkNotNull(button.getId());
  }
}
