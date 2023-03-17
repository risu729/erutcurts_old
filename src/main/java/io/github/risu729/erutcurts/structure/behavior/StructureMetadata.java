/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.risu729.erutcurts.structure.nbt.Coordinate;
import io.github.risu729.erutcurts.structure.nbt.Size;
import io.github.risu729.erutcurts.structure.nbt.Structure;
import io.github.risu729.erutcurts.util.gson.SemverTypeAdapter;
import java.util.List;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@With
record StructureMetadata(
    @NotNull Identifier identifier,
    @NotNull Semver minEngineVersion,
    @NotNull Size size,
    @Nullable Coordinate coordinate) {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(Semver.class, SemverTypeAdapter.newInstance(true, semver -> false))
          .create();

  StructureMetadata(@NotNull Identifier identifier, @NotNull Structure structure) {
    this(identifier, structure.getMinEngineVersion(), structure.size(), null);
  }

  static @NotNull String toJson(@NotNull List<@NotNull StructureMetadata> src) {
    return GSON.toJson(src);
  }
}
