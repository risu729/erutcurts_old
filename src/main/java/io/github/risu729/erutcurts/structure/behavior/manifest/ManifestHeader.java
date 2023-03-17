/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.annotations.Expose;
import java.util.UUID;
import lombok.Builder;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@Builder(toBuilder = true)
@With
public record ManifestHeader(
    @Expose(deserialize = false, serialize = false) @NotNull ManifestModule.Type type,
    @NotNull String name,
    @Nullable String description,
    @NotNull UUID uuid,
    @NotNull Semver version,
    @Nullable Semver minEngineVersion,
    @Nullable Boolean platformLocked,
    @Nullable PackScope packScope,
    @Nullable Semver baseGameVersion,
    @Nullable Boolean lockTemplateOptions) {

  public static final Semver LOWEST_GAME_VERSION = Semver.parse("1.13.0");

  public ManifestHeader {
    checkArgument(!name.isBlank());
    switch (type) {
      case RESOURCES -> {
        checkNotNull(minEngineVersion);
        checkArgument(baseGameVersion == null);
        checkArgument(lockTemplateOptions == null);
      }
      case DATA, INTERFACE, SCRIPT -> {
        checkNotNull(minEngineVersion);
        checkArgument(minEngineVersion.isGreaterThanOrEqualTo(LOWEST_GAME_VERSION));
        checkArgument(packScope == null);
        checkArgument(baseGameVersion == null);
        checkArgument(lockTemplateOptions == null);
      }
      case SKIN_PACK -> {
        checkArgument(minEngineVersion == null);
        checkArgument(platformLocked == null);
        checkArgument(packScope == null);
        checkArgument(baseGameVersion == null);
        checkArgument(lockTemplateOptions == null);
      }
      case WORLD_TEMPLATE -> {
        checkArgument(minEngineVersion == null);
        checkArgument(platformLocked == null);
        checkArgument(packScope == null);
        checkNotNull(baseGameVersion);
        checkArgument(baseGameVersion.isGreaterThanOrEqualTo(LOWEST_GAME_VERSION));
        checkNotNull(lockTemplateOptions);
      }
    }
  }

  public enum PackScope {
    GLOBAL,
    WORLD
  }

  @SuppressWarnings({"FieldMayBeFinal", "unused"})
  public static final class ManifestHeaderBuilder {

    private UUID uuid = UUID.randomUUID();
    private Semver version = Semver.parse("1.0.0");
  }
}
