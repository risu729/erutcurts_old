/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.With;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@SuppressWarnings({"unused", "WeakerAccess"})
@Builder(toBuilder = true)
@With
public record ManifestDependency(
    @Nullable UUID uuid, @Nullable String moduleName, @NotNull Semver version) {

  public ManifestDependency {
    checkArgument(uuid == null ^ moduleName == null);
    checkArgument(moduleName == null || !moduleName.isBlank());
  }

  @Contract(pure = true)
  public static @NotNull ManifestDependency fromModule(@NotNull ManifestModule module) {
    return builder().uuid(module.uuid()).version(module.version()).build();
  }

  @Contract(pure = true)
  public static @NotNull Set<@NotNull ManifestDependency> fromManifest(@NotNull Manifest manifest) {
    return manifest.modules().stream()
        .map(ManifestDependency::fromModule)
        .collect(Collectors.toUnmodifiableSet());
  }
}
