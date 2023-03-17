/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.reflect.TypeToken;
import io.github.risu729.erutcurts.DiscordDB;
import io.github.risu729.erutcurts.misc.DataRequest;
import io.github.risu729.erutcurts.misc.Notifications;
import java.util.List;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import nl.itslars.mcpenbt.enums.TagType;
import nl.itslars.mcpenbt.tags.CompoundTag;
import nl.itslars.mcpenbt.tags.IntTag;
import nl.itslars.mcpenbt.tags.ListTag;
import nl.itslars.mcpenbt.tags.StringTag;
import nl.itslars.mcpenbt.tags.Tag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

@UtilityClass
public class LevelVersions {

  final String FLAT_WORLD_LAYERS_KEY = "FlatWorldLayers";
  private final String GENERATOR_KEY = "Generator";

  @SuppressWarnings("FieldNamingConvention")
  private final String MINIMUM_COMPATIBLE_CLIENT_VERSION_KEY = "MinimumCompatibleClientVersion";

  private final String DATABASE_NAME = "LevelVersions";
  private final String WORLD_VERSION_KEY = "WorldVersion";
  private final String INVENTORY_VERSION_KEY = "InventoryVersion";
  private final String STORAGE_VERSION_KEY = "StorageVersion";
  private final String NETWORK_VERSION_KEY = "NetworkVersion";

  @CheckReturnValue
  private @NotNull LevelVersionsData get() {
    var optional = DiscordDB.get(DATABASE_NAME, TypeToken.get(LevelVersionsData.class));
    if (optional.isEmpty()) {
      Notifications.sendDataRequest(DataRequest.EXPORTED_FLAT_WORLD);
      throw new IllegalStateException("LevelVersionsData is not present");
    }
    return optional.orElseThrow();
  }

  public void update(@NotNull CompoundTag level) {
    var flatWorldLayers =
        Levels.FlatWorldLayers.fromJson(
            level
                .getByName(FLAT_WORLD_LAYERS_KEY)
                .map(Tag::getAsString)
                .map(StringTag::getValue)
                .orElseThrow());
    DiscordDB.put(
        DATABASE_NAME,
        LevelVersionsData.builder()
            .generator(
                level
                    .getByName(GENERATOR_KEY)
                    .map(Tag::getAsInt)
                    .map(IntTag::getValue)
                    .orElseThrow())
            .minimumCompatibleClientVersion(
                level
                    .getByName(MINIMUM_COMPATIBLE_CLIENT_VERSION_KEY)
                    .map(Tag::<IntTag>getAsList)
                    .map(ListTag::getElements)
                    .stream()
                    .flatMap(List::stream)
                    .map(IntTag::getValue)
                    .toList())
            .worldVersion(
                level
                    .getByName(WORLD_VERSION_KEY)
                    .map(Tag::getAsInt)
                    .map(IntTag::getValue)
                    .orElseThrow())
            .inventoryVersion(
                checkNotNull(
                    Semver.parse(
                        level
                            .getByName(INVENTORY_VERSION_KEY)
                            .map(Tag::getAsString)
                            .map(StringTag::getValue)
                            .orElseThrow())))
            .storageVersion(
                level
                    .getByName(STORAGE_VERSION_KEY)
                    .map(Tag::getAsInt)
                    .map(IntTag::getValue)
                    .orElseThrow())
            .networkVersion(
                level
                    .getByName(NETWORK_VERSION_KEY)
                    .map(Tag::getAsInt)
                    .map(IntTag::getValue)
                    .orElseThrow())
            .flatWorldLayers(
                LevelVersions.LevelVersionsData.FlatWorldLayersVersionsData.builder()
                    .encodingVersion(flatWorldLayers.encodingVersion())
                    .worldVersion(flatWorldLayers.worldVersion())
                    .build())
            .build());
  }

  @CheckReturnValue
  @NotNull
  IntTag generator() {
    return new IntTag(GENERATOR_KEY, get().generator());
  }

  @CheckReturnValue
  @NotNull
  ListTag<@NotNull IntTag> minimumCompatibleClientVersion() {
    return new ListTag<>(
        MINIMUM_COMPATIBLE_CLIENT_VERSION_KEY,
        TagType.TAG_INT,
        get().minimumCompatibleClientVersion().stream()
            .map(value -> new IntTag(null, value))
            .toList());
  }

  @CheckReturnValue
  @NotNull
  IntTag worldVersion() {
    return new IntTag(WORLD_VERSION_KEY, get().worldVersion());
  }

  @CheckReturnValue
  @NotNull
  StringTag inventoryVersion() {
    return new StringTag(INVENTORY_VERSION_KEY, get().inventoryVersion().toString());
  }

  @CheckReturnValue
  @NotNull
  IntTag storageVersion() {
    return new IntTag(STORAGE_VERSION_KEY, get().storageVersion());
  }

  @CheckReturnValue
  @NotNull
  IntTag networkVersion() {
    return new IntTag(NETWORK_VERSION_KEY, get().networkVersion());
  }

  @CheckReturnValue
  int flatWorldLayersEncodingVersion() {
    return get().flatWorldLayers().encodingVersion();
  }

  @CheckReturnValue
  @NotNull
  String flatWorldLayersWorldVersion() {
    return get().flatWorldLayers().worldVersion();
  }

  // values in level.dat that might change in the future
  // this need to be updated when new MCBE version is released
  @Builder
  private record LevelVersionsData(
      int generator,
      // use List instead of Semver because it may have more than 3 numbers
      @NotNull List<@NotNull Integer> minimumCompatibleClientVersion,
      int worldVersion,
      @NotNull Semver inventoryVersion,
      int storageVersion,
      int networkVersion,
      @NotNull LevelVersions.LevelVersionsData.FlatWorldLayersVersionsData flatWorldLayers) {

    private LevelVersionsData {
      checkArgument(generator > 0);
      checkArgument(minimumCompatibleClientVersion.stream().allMatch(i -> i >= 0));
      checkArgument(minimumCompatibleClientVersion.size() == 5);
      checkArgument(worldVersion > 0);
      checkArgument(storageVersion > 0);
      checkArgument(networkVersion > 0);
    }

    @Builder
    private record FlatWorldLayersVersionsData(int encodingVersion, @NotNull String worldVersion) {

      private FlatWorldLayersVersionsData {
        checkArgument(encodingVersion > 0);
        checkArgument(!worldVersion.isEmpty());
      }
    }
  }
}
