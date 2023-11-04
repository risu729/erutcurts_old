/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.blueprint;

import io.github.risu729.erutcurts.structure.nbt.Structure;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.io.IOException;
import java.nio.file.Files;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class Blueprint {

  @CheckReturnValue
  public static @NotNull CloseablePath generate(
      @NotNull String filename, @NotNull Structure structure) throws IOException {
    var tempDir = FileUtil.createTempDir();
    try (var closeableImagesDir =
        CloseablePath.of(Files.createDirectory(tempDir.resolve(filename)))) {
      var imagesDir = closeableImagesDir.path();

      //      structure.blockIndices().stream().close();

      return CloseablePath.of(
          FileUtil.zip(tempDir.resolve(filename + "_blueprints.zip"), imagesDir, false), true);
    }
  }

//  private static @NotNull Path getTexturePath(@NotNull String blockName) throws IOException {
//    return Block.fromJson(Files.readString(BLOCKS_JSON)).get(blockName.replace("minecraft:", ""));
//  }
}
