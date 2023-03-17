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
import io.github.risu729.erutcurts.structure.behavior.manifest.Manifest;
import io.github.risu729.erutcurts.util.gson.SemverTypeAdapter;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

record BehaviorPack(@NotNull UUID packId, @NotNull Semver version) {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(Semver.class, SemverTypeAdapter.newInstance(true, semver -> true))
          .create();

  @Contract(pure = true)
  static @NotNull BehaviorPack fromManifest(@NotNull Manifest manifest) {
    return new BehaviorPack(manifest.header().uuid(), manifest.header().version());
  }

  @Contract(pure = true)
  static @NotNull String toJson(@NotNull List<@NotNull BehaviorPack> src) {
    return GSON.toJson(src);
  }
}
