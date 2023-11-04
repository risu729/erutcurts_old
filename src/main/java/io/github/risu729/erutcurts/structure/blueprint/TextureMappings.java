/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.blueprint;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.risu729.erutcurts.Erutcurts;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class TextureMappings {

  private final Path RESOURCE_PACK_DIR =
      Erutcurts.RESOURCES_DIR.resolve(Path.of("bedrock-samples", "resource_pack"));
  private final Path BLOCKS_JSON = RESOURCE_PACK_DIR.resolve("blocks.json");
  private final Path TERRAIN_TEXTURE =
      RESOURCE_PACK_DIR.resolve(Path.of("textures", "terrain_texture.json"));

  private final Gson GSON = new Gson();

  public @Nullable Path getTexturePath(@NotNull String blockName) throws IOException {
    return readBlocksJson().get(blockName);
  }

  private @NotNull Map<@NotNull String, @NotNull List<@NotNull Textures>> readBlocksJson()
      throws IOException {

    var terrainTexture = readTerrainTexture();

    var blocks = GSON.fromJson(Files.readString(BLOCKS_JSON), JsonObject.class);
    blocks.remove("format_version");
    return blocks.entrySet().stream()
        .map(
            entry -> {
              List<Textures> textures;
              var texturesElement = entry.getValue().getAsJsonObject().get("textures");
              if (texturesElement == null) {
                textures = List.of(Textures.INVISIBLE);
              } else if (texturesElement.isJsonPrimitive()
                  && texturesElement.getAsJsonPrimitive().isString()) {
                textures =
                    terrainTexture.get(texturesElement.getAsString()).stream()
                        .map(Textures::new)
                        .toList();

              } else if (texturesElement.isJsonObject()) {
                var texturesObj = texturesElement.getAsJsonObject();

                var texturesList =
                    Textures.TEXTURE_KEYS.stream()
                        .filter(texturesObj::has)
                        .collect(
                            Collectors.toUnmodifiableMap(
                                Function.identity(),
                                key -> terrainTexture.get(texturesObj.get(key).getAsString())));

                var variants = texturesList.values().stream().mapToInt(List::size).max().orElse(0);

                textures = new ArrayList<>(variants);

                for (int i = 0; i < variants; i++) {
                  var all = texturesList.containsKey("all") ? texturesList.get("all").get(i) : null;
                }

              } else {
                throw new IllegalStateException("Unexpected value: " + texturesElement);
              }
              return Map.entry(entry.getKey(), textures);
            })
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private @NotNull Map<@NotNull String, @NotNull List<@NotNull Path>> readTerrainTexture()
      throws IOException {
    var terrainTexture = GSON.fromJson(Files.readString(TERRAIN_TEXTURE), JsonObject.class);
    return checkNotNull(terrainTexture.get("texture_data")).getAsJsonObject().entrySet().stream()
        .map(
            entry ->
                Map.entry(
                    entry.getKey(),
                    Stream.of(
                            entry.getValue().isJsonPrimitive()
                                ? entry.getValue()
                                : entry.getValue().getAsJsonArray())
                        .map(JsonElement::getAsString)
                        .map(Path::of)
                        .map(RESOURCE_PACK_DIR::resolve)
                        .toList()))
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private record Textures(
      @Nullable Path all,
      @Nullable Path up,
      @Nullable Path side,
      @Nullable Path north,
      @Nullable Path east,
      @Nullable Path south,
      @Nullable Path west,
      @Nullable Path down) {

    private static final List<String> TEXTURE_KEYS =
        List.of("up", "side", "north", "east", "south", "west", "down");

    private static final Textures INVISIBLE =
        new Textures(null, null, null, null, null, null, null, null);

    private Textures(@NotNull Path all) {
      this(all, null, null, null, null, null, null, null);
    }

    @SuppressWarnings("VariableNotUsedInsideIf")
    private Textures {
      if (all != null) {
        checkState(Stream.of(up, side, north, east, south, west, down).allMatch(Objects::isNull));
      }
    }
  }
}
