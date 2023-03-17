/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.risu729.erutcurts.util.gson.EmptyCollectionTypeAdapterFactory;
import io.github.risu729.erutcurts.util.gson.SemverTypeAdapter;
import io.github.risu729.erutcurts.util.gson.SnakeCaseEnumTypeAdapterFactory;
import io.github.risu729.erutcurts.util.gson.TypeAdapters;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@SuppressWarnings("unused")
@Builder(toBuilder = true)
@With
public record Manifest(
    int formatVersion,
    @NotNull ManifestHeader header,
    @Singular @NotNull List<@NotNull ManifestModule> modules,
    // optional list components are nullable
    // because Gson returns null if the value is not present
    @Singular @Nullable List<@NotNull ManifestDependency> dependencies,
    @Singular @Nullable Set<@NotNull ManifestCapability> capabilities,
    @NotNull ManifestMetadata metadata,
    @Singular @Nullable List<@NotNull ManifestSubpack> subpacks) {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapterFactory(SnakeCaseEnumTypeAdapterFactory.newInstance())
          .registerTypeAdapterFactory(EmptyCollectionTypeAdapterFactory.newInstance())
          .registerTypeHierarchyAdapter(Path.class, TypeAdapters.newPathTypeAdapter())
          .registerTypeAdapter(
              Semver.class,
              SemverTypeAdapter.newInstance(
                  true, semver -> semver.getPreRelease().isEmpty() && semver.getBuild().isEmpty()))
          .create();

  public Manifest {
    checkArgument(formatVersion > 0);
    if (formatVersion != 2) {
      throw new UnsupportedOperationException("Only format version 2 is supported.");
    }

    checkArgument(!modules.isEmpty());
    var moduleTypes = modules.stream().map(ManifestModule::type).distinct().toList();
    checkState(
        moduleTypes.stream().allMatch(type -> type.getCompatibleTypes().containsAll(moduleTypes)),
        "Incompatible module types: %s",
        moduleTypes);

    switch (header.type()) {
      case DATA, INTERFACE, SCRIPT -> checkArgument(subpacks == null || subpacks.isEmpty());
      case SKIN_PACK, WORLD_TEMPLATE -> {
        checkArgument(dependencies == null || dependencies.isEmpty());
        checkArgument(capabilities == null || capabilities.isEmpty());
        checkArgument(subpacks == null || subpacks.isEmpty());
      }
    }

    checkArgument(subpacks == null || subpacks.size() != 1);
  }

  @Contract(pure = true)
  public static @NotNull Manifest fromJson(@NotNull String json) {
    return GSON.fromJson(json, Manifest.class);
  }

  @Contract(pure = true)
  public @NotNull String toJson() {
    return GSON.toJson(this);
  }

  @Contract(pure = true)
  public @NotNull List<@NotNull ManifestDependency> dependencies() {
    return dependencies == null ? List.of() : dependencies;
  }

  @Contract(pure = true)
  public @NotNull Set<@NotNull ManifestCapability> capabilities() {
    return capabilities == null ? Set.of() : capabilities;
  }

  @Contract(pure = true)
  public @NotNull List<@NotNull ManifestSubpack> subpacks() {
    return subpacks == null ? List.of() : subpacks;
  }

  // to set default values
  @SuppressWarnings({"FieldMayBeFinal", "unused"})
  public static class ManifestBuilder {

    private int formatVersion = 2;
  }
}
