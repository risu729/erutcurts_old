/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkArgument;

import io.github.risu729.erutcurts.structure.MCExtension;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// based on "Bedrock mcstructure file format" by tryashtar
// https://gist.github.com/tryashtar/87ad9654305e5df686acab05cc4b6205

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class Identifier {

  // use of an explicit "mystructure" directory is restricted because Minecraft produces a warning
  private static final String DEFAULT_NAMESPACE = "mystructure";
  private static final String NAMESPACE_DELIMITER = ":";

  @SuppressWarnings("HardcodedFileSeparator")
  private static final String PATH_DELIMITER = "/";

  private static final Pattern IDENTIFIER_PATTERN =
      Pattern.compile("^(?:[^:/]+:)?(?:[^:/]+/)*[^:/]+");

  @NotNull String namespace;
  @NotNull List<@NotNull String> path;

  private Identifier(@Nullable String namespace, @NotNull List<String> path) {
    this.namespace = namespace == null ? DEFAULT_NAMESPACE : namespace;
    checkArgument(!this.namespace.isBlank());
    checkArgument(!path.isEmpty());
    this.path = List.copyOf(path);
    if (isDefaultNamespace()) {
      checkArgument(
          path.size() == 1, "Use of an explicit \"mystructure\" directory is restricted.");
    }
  }

  @Contract(pure = true)
  public static @NotNull Identifier fromString(@NotNull String identifier) {
    checkArgument(IDENTIFIER_PATTERN.matcher(identifier).matches());
    if (identifier.contains(NAMESPACE_DELIMITER)) {
      var index = identifier.indexOf(NAMESPACE_DELIMITER);
      return new Identifier(
          identifier.substring(0, index),
          List.of(identifier.substring(index + 1).split(PATH_DELIMITER)));
    } else {
      return new Identifier(null, List.of(identifier.split(PATH_DELIMITER)));
    }
  }

  @Contract(pure = true)
  private boolean isDefaultNamespace() {
    return namespace.equals(DEFAULT_NAMESPACE);
  }

  @Contract(pure = true)
  @NotNull
  Path toPath() {
    return toPath(MCExtension.MCSTRUCTURE.toString());
  }

  @Contract(pure = true)
  @NotNull
  Path toPath(@NotNull String extension) {
    return FileUtil.appendExtension(
        isDefaultNamespace()
            ? Path.of(path.get(0))
            : Path.of(namespace, path.toArray(String[]::new)),
        extension);
  }

  @Contract(pure = true)
  @NotNull
  String toStringWithoutDefaultNamespace() {
    if (!isDefaultNamespace()) {
      return toString();
    }
    return String.join(PATH_DELIMITER, path);
  }

  @Override
  public @NotNull String toString() {
    return namespace + NAMESPACE_DELIMITER + String.join(PATH_DELIMITER, path);
  }
}
