/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure;

import java.util.Locale;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MCExtension {
  MCADDON,
  MCFUNCTION,
  MCPACK,
  MCPERF,
  MCSHORTCUT,
  MCSTRUCTURE,
  MCTEMPLATE,
  MCWORLD,
  NBT,
  DAT;

  @NotNull String str;

  MCExtension() {
    str = name().toLowerCase(Locale.ENGLISH);
  }

  @Override
  public @NotNull String toString() {
    return str;
  }
}
