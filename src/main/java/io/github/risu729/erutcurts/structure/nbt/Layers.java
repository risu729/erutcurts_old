/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.nbt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public record Layers(@Nullable Block primary, @Nullable Block secondary) {

  @Contract(pure = true)
  public boolean isVoid() {
    return primary == null && secondary == null;
  }
}
