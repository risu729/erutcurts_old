/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.file;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// do not implement Path because the instance of Path is OS dependent
@Value
@AllArgsConstructor(staticName = "of")
@Accessors(fluent = true)
public class CloseablePath implements AutoCloseable {

  @NotNull Path path;
  boolean deleteParentOnClose;

  @Contract(pure = true)
  public static @NotNull CloseablePath of(@NotNull Path path) {
    return of(path, false);
  }

  @Override
  public void close() {
    FileUtil.deleteQuietly(path);
    if (deleteParentOnClose) {
      FileUtil.deleteIfEmptyQuietly(path.getParent());
    }
  }
}
