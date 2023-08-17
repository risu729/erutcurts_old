/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// wrap env variables for null checking
@UtilityClass
public class Envs {

  private final Dotenv DOTENV = Dotenv.load();

  @Contract(pure = true)
  public @NotNull String getEnv(@NotNull String key) {
    return checkNotNull(DOTENV.get(key), "Environment variable %s is not set", key);
  }
}
