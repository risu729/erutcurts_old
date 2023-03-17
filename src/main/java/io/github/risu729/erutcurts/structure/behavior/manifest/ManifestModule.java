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

import com.google.gson.annotations.SerializedName;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@Builder(toBuilder = true)
@With
public record ManifestModule(
    @NotNull Type type,
    @Nullable String description,
    @NotNull UUID uuid,
    @NotNull Semver version,
    @Nullable Path entry,
    @Nullable Language language) {

  public ManifestModule {
    if (type == Type.SCRIPT) {
      checkNotNull(entry);
      checkNotNull(language);
      checkArgument(Path.of("scripts").equals(entry.getParent()));
      checkArgument(FileUtil.isExtension(entry, language.extension()));
    } else {
      checkArgument(entry == null);
      checkArgument(language == null);
    }
  }

  public enum Type {
    DATA, // behavior pack
    INTERFACE, // unknown
    RESOURCES, // resource pack
    SCRIPT, // GameTest Framework
    SKIN_PACK, // skin pack
    WORLD_TEMPLATE; // world template

    @NotNull
    Set<@NotNull Type> getCompatibleTypes() {
      return switch (this) {
        case DATA, INTERFACE, SCRIPT -> Set.of(DATA, INTERFACE, SCRIPT);
        case RESOURCES -> Set.of(RESOURCES);
        case SKIN_PACK -> Set.of(SKIN_PACK);
        case WORLD_TEMPLATE -> Set.of(WORLD_TEMPLATE);
      };
    }
  }

  @Getter
  @Accessors(fluent = true)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public enum Language {
    @SerializedName("JavaScript")
    JAVASCRIPT("js");

    @NotNull String extension;
  }

  @SuppressWarnings({"FieldMayBeFinal", "unused"})
  public static final class ManifestModuleBuilder {

    private UUID uuid = UUID.randomUUID();
    private Semver version = Semver.parse("1.0.0");
  }
}
