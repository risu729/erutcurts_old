/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// wrap env variables to keep it consistent during runtime and for null checking
@UtilityClass
public class Envs {

  @Contract(pure = true)
  public @NotNull String getEnv(@NotNull String key) {
    return checkNotNull(System.getenv(key), "Environment variable %s is not set", key);
  }
}
