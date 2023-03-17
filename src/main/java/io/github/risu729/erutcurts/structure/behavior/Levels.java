/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.risu729.erutcurts.Erutcurts;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;
import nl.itslars.mcpenbt.NBTUtil;
import nl.itslars.mcpenbt.tags.CompoundTag;
import nl.itslars.mcpenbt.tags.LongTag;
import nl.itslars.mcpenbt.tags.StringTag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
class Levels {

  private final Path TEMPLATE_LEVEL = Erutcurts.RESOURCES_DIR.resolve("template_level.dat");

  @CheckReturnValue
  @NotNull
  CompoundTag generateLevelDat(
      @NotNull String levelName, @NotNull FlatWorldLayers flatWorldLayers) {
    var level = NBTUtil.read(true, TEMPLATE_LEVEL).getAsCompound();
    // overwrite tags
    List.of(
            new StringTag("LevelName", levelName),
            new StringTag(LevelVersions.FLAT_WORLD_LAYERS_KEY, flatWorldLayers.toJson()),
            new LongTag("LastPlayed", OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()),
            LevelVersions.generator(),
            LevelVersions.minimumCompatibleClientVersion(),
            LevelVersions.worldVersion(),
            LevelVersions.inventoryVersion(),
            LevelVersions.storageVersion(),
            LevelVersions.networkVersion())
        .forEach(tag -> level.change(tag.getName(), tag));
    return level;
  }

  record FlatWorldLayers(
      @NotNull List<@NotNull BlockLayer> blockLayers,
      int biomeId,
      @Nullable Void structureOptions,
      int encodingVersion,
      @NotNull String worldVersion) {

    private static final int DEFAULT_BIOME_ID = 1;

    private static final Gson GSON =
        new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    FlatWorldLayers(
        @NotNull List<@NotNull BlockLayer> blockLayers,
        int biomeId,
        @Nullable Void structureOptions) {
      this(
          blockLayers,
          biomeId,
          structureOptions,
          LevelVersions.flatWorldLayersEncodingVersion(),
          LevelVersions.flatWorldLayersWorldVersion());
    }

    FlatWorldLayers {
      checkArgument(encodingVersion > 0);
    }

    @Contract(pure = true)
    static @NotNull FlatWorldLayers newVoid() {
      return new FlatWorldLayers(Collections.emptyList(), DEFAULT_BIOME_ID, null);
    }

    @Contract(pure = true)
    static @NotNull FlatWorldLayers fromJson(@NotNull String json) {
      return GSON.fromJson(json, FlatWorldLayers.class);
    }

    @NotNull
    String toJson() {
      return GSON.toJson(this);
    }

    record BlockLayer(@NotNull String blockName, int count) {

      BlockLayer {
        checkArgument(count > 0);
      }
    }
  }
}
