/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Path;
import lombok.Builder;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Builder(toBuilder = true)
@With
public record ManifestSubpack(
    @NotNull Path folderName, @NotNull String name, @Nullable Integer memoryTier) {

  public ManifestSubpack(@NotNull Path folderName, @NotNull String name) {
    this(folderName, name, null);
  }

  public ManifestSubpack {
    checkArgument(folderName.getNameCount() == 1);
    checkArgument(!name.isBlank());
    checkArgument(memoryTier == null || memoryTier >= 0);
  }
}
