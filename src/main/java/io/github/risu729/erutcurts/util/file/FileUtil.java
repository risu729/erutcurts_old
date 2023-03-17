/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.file;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import io.github.risu729.erutcurts.Erutcurts;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"WeakerAccess", "unused"})
@UtilityClass
public class FileUtil {

  public void deleteQuietly(@NotNull Path path) {
    if (Files.exists(path)) {
      try {
        MoreFiles.deleteRecursively(path, RecursiveDeleteOption.ALLOW_INSECURE);
      } catch (IOException ignored) {
      }
    }
  }

  public void deleteIfEmptyQuietly(@NotNull Path dir) {
    if (Files.exists(dir)) {
      checkArgument(Files.isDirectory(dir));
      try (var stream = Files.list(dir)) {
        if (stream.findAny().isEmpty()) {
          Files.delete(dir);
        }
      } catch (IOException ignored) {
      }
    }
  }

  public @NotNull Path copyToDir(@NotNull Path source, @NotNull Path targetDir) throws IOException {
    return Files.copy(source, targetDir.resolve(source.getFileName()));
  }

  @SuppressWarnings("UnusedReturnValue")
  public @NotNull Path createDirectoriesAndCopy(@NotNull Path source, @NotNull Path target)
      throws IOException {
    var parent = target.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    return Files.copy(source, target);
  }

  @SuppressWarnings("UnusedReturnValue")
  public @NotNull Path createDirectoriesAndWriteString(@NotNull Path path, @NotNull String str)
      throws IOException {
    var parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    return Files.writeString(path, str);
  }

  @Contract(pure = true)
  public @NotNull String getFileExtension(@NotNull String filename) {
    return MoreFiles.getFileExtension(Path.of(filename));
  }

  @Contract(pure = true)
  public @NotNull String getFilenameWithoutExtension(@NotNull String filename) {
    return MoreFiles.getNameWithoutExtension(Path.of(filename));
  }

  @SuppressWarnings({"MethodCallInLoopCondition", "ReassignedVariable"})
  @CheckReturnValue
  public @NotNull Path generateUniquePathInDir(@NotNull Path path) {
    if (!Files.exists(path)) {
      return path;
    }

    var filename = path.getFileName();
    var nameWithoutExtension = MoreFiles.getNameWithoutExtension(filename);
    var extension = "." + MoreFiles.getFileExtension(filename);

    var generatedPath = path;
    var dir = path.getParent();
    for (int i = 1; Files.exists(generatedPath); i++) {
      generatedPath = dir.resolve(nameWithoutExtension + "_" + i + extension);
    }
    return path;
  }

  @CheckReturnValue
  public @NotNull Path createTempDir() {
    try {
      return Files.createTempDirectory(Erutcurts.TEMP_DIR, null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @SuppressWarnings("OverloadedVarargsMethod")
  @Contract(pure = true)
  public boolean isExtension(@NotNull Path path, @NotNull String @NotNull ... extensions) {
    return isExtension(path.getFileName().toString(), extensions);
  }

  @SuppressWarnings("OverloadedVarargsMethod")
  @Contract(pure = true)
  public boolean isExtension(@NotNull String filename, @NotNull String @NotNull ... extensions) {
    var extension = MoreFiles.getFileExtension(Path.of(filename)).toLowerCase(Locale.ENGLISH);
    return Arrays.asList(extensions).contains(extension);
  }

  @Contract(pure = true)
  public @NotNull Path appendExtension(@NotNull Path path, @NotNull String extension) {
    return Path.of(path + "." + extension);
  }

  @Contract("_, _, _ -> param1")
  @CheckReturnValue
  public Path zip(@NotNull Path target, @NotNull Path directory, boolean isInDirectory)
      throws IOException {
    checkArgument(Files.notExists(target), "Target file already exists");
    checkArgument(Files.isDirectory(directory), "The directory is not a directory");
    try (var zip = new ZipFile(target.toFile())) {
      if (isInDirectory) {
        zip.addFolder(directory.toFile());
      } else {
        List<Path> list;
        try (var stream = Files.list(directory)) {
          list = stream.toList();
        }
        for (var element : list) {
          if (Files.isDirectory(element)) {
            zip.addFolder(element.toFile());
          } else {
            zip.addFile(element.toFile());
          }
        }
      }
    }
    return target;
  }
}
