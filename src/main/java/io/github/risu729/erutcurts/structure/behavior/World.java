/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.structure.MCExtension;
import io.github.risu729.erutcurts.structure.nbt.Coordinate;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.experimental.Accessors;
import nl.itslars.mcpenbt.NBTUtil;
import nl.itslars.mcpenbt.enums.HeaderType;
import nl.itslars.mcpenbt.tags.CompoundTag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.semver4j.Semver;

@Value
@Accessors(fluent = true)
public class World {

  public static final Path LEVEL_FILENAME =
      FileUtil.appendExtension(Path.of("level"), MCExtension.DAT.toString());
  private static final Path WORLD_ICON_FILENAME = Path.of("world_icon.jpeg");
  private static final Path WORLD_BEHAVIOR_PACKS_FILENAME = Path.of("world_behavior_packs.json");
  private static final Path BEHAVIOR_PACKS_DIR_NAME = Path.of("behavior_packs");
  private static final Path FUNCTIONS_DIR_NAME = Path.of("functions");
  private static final Path TICK_FILENAME = Path.of("tick.json");
  private static final Path FIRST_LOAD_FILENAME =
      FileUtil.appendExtension(
          Path.of("internal", "first_load"), MCExtension.MCFUNCTION.toString());
  private static final Path RELOAD_STRUCTURES_FILENAME =
      Path.of("reload_all_structures" + '.' + MCExtension.MCFUNCTION);
  private static final Path RELOAD_DIR_NAME = Path.of("reload");

  private static final Path FIRST_LOAD_FUNCTION =
      Erutcurts.RESOURCES_DIR.resolve("first_load_function.mcfunction");
  private static final Path DEFAULT_WORLD_ICON =
      Erutcurts.RESOURCES_DIR.resolve("default_world_icon.jpg");

  private static final Semver NEW_EXECUTE_MIN_ENGINE_VERSION = Semver.parse("1.19.50");
  private static final int STRUCTURES_GAP = 3;
  private static final int STRUCTURES_Y_COORDINATE = 0;

  @NotNull String worldName;
  @NotNull Path worldIcon;
  @NotNull CompoundTag level;
  @NotNull List<@NotNull BehaviorPack> worldBehaviorPacks;
  @NotNull Behavior behavior;
  @NotNull TickFunctions tickFunctions;
  @NotNull String reloadStructuresFunction;
  @NotNull Map<@NotNull Identifier, @NotNull String> structureFunctions;

  @SuppressWarnings({"HardcodedLineSeparator", "HardcodedFileSeparator"})
  public World(
      @Nullable String worldName,
      @Nullable Path worldIcon,
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures) {
    this.worldName =
        worldName == null
            ? structures.keySet().stream()
                .findFirst()
                .map(Identifier::toStringWithoutDefaultNamespace)
                .orElseThrow()
            : worldName;
    this.worldIcon = worldIcon == null ? DEFAULT_WORLD_ICON : worldIcon;

    var baseBehavior = new Behavior(null, null, structures);
    var baseManifest = baseBehavior.manifest();
    var structureMinEngine = checkNotNull(baseManifest.header().minEngineVersion());
    this.behavior =
        Behavior.builder()
            .packName(baseBehavior.packName())
            .packIcon(baseBehavior.packIcon())
            .manifest(
                structureMinEngine.isLowerThan(NEW_EXECUTE_MIN_ENGINE_VERSION)
                    ? baseManifest.withHeader(
                        baseManifest.header().withMinEngineVersion(NEW_EXECUTE_MIN_ENGINE_VERSION))
                    : baseManifest)
            .structureMetadata(addCoordinateToMetadata(baseBehavior.structureMetadata()))
            .structures(baseBehavior.structures())
            .build();

    this.level =
        Levels.generateLevelDat(
            "Structures: %s".formatted(this.worldName), Levels.FlatWorldLayers.newVoid());
    this.worldBehaviorPacks = List.of(BehaviorPack.fromManifest(behavior.manifest()));
    this.tickFunctions = new TickFunctions(FIRST_LOAD_FILENAME);

    this.structureFunctions =
        behavior.structureMetadata().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    StructureMetadata::identifier,
                    metadata -> {
                      var coordinate = checkNotNull(metadata.coordinate());
                      return "structure load %s %d %d %d"
                          .formatted(
                              metadata.identifier(),
                              coordinate.x(),
                              coordinate.y(),
                              coordinate.z());
                    }));
    this.reloadStructuresFunction =
        structureFunctions.keySet().stream()
            .map(Identifier::toStringWithoutDefaultNamespace)
            .map("function reload/%s"::formatted)
            .collect(Collectors.joining("\n"));
  }

  @CheckReturnValue
  public static @NotNull CloseablePath generate(
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures) throws IOException {
    return generate(null, null, structures);
  }

  @CheckReturnValue
  public static @NotNull CloseablePath generate(
      @Nullable String worldName,
      @Nullable Path worldIcon,
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures)
      throws IOException {
    var tempDir = FileUtil.createTempDir();
    try (var closeableWorldDir =
        CloseablePath.of(new World(worldName, worldIcon, structures).makeDir(tempDir))) {
      var worldDir = closeableWorldDir.path();
      return CloseablePath.of(
          FileUtil.zip(
              tempDir.resolve(
                  FileUtil.appendExtension(worldDir.getFileName(), MCExtension.MCWORLD.toString())),
              worldDir,
              false),
          true);
    }
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Contract(pure = true)
  private @NotNull @Unmodifiable List<@NotNull StructureMetadata> addCoordinateToMetadata(
      @NotNull List<@NotNull StructureMetadata> structureMetadata) {
    var spacing =
        structureMetadata.stream()
                .map(StructureMetadata::size)
                .mapMultiToInt(
                    (size, consumer) -> {
                      consumer.accept(size.x());
                      consumer.accept(size.z());
                    })
                .max()
                .orElseThrow()
            + STRUCTURES_GAP;
    var structuresInRow = (int) Math.ceil(Math.sqrt(structureMetadata.size()));
    Iterator<StructureMetadata> iterator =
        structureMetadata.stream()
            .sorted(Comparator.comparing(metadata -> metadata.identifier().toString()))
            .iterator();
    int edgeCoordinate = (int) Math.floor((double) structuresInRow / 2) * -spacing;
    List<StructureMetadata> newMetadata = new ArrayList<>();
    int x = edgeCoordinate;
    x:
    for (int xI = 0; xI < structuresInRow; xI++, x += spacing) {
      int z = edgeCoordinate;
      for (int zI = 0; zI < structuresInRow; zI++, z += spacing) {
        var metadata = iterator.next();
        newMetadata.add(metadata.withCoordinate(new Coordinate(x, STRUCTURES_Y_COORDINATE, z)));
        if (!iterator.hasNext()) {
          break x;
        }
      }
    }
    return Collections.unmodifiableList(newMetadata);
  }

  @CheckReturnValue
  private @NotNull Path makeDir(@NotNull Path parent) throws IOException {
    var worldDir = Files.createDirectory(parent.resolve(worldName));

    Files.write(worldDir.resolve(LEVEL_FILENAME), NBTUtil.write(level, HeaderType.LEVEL_DAT));
    Files.copy(worldIcon, worldDir.resolve(WORLD_ICON_FILENAME));
    Files.writeString(
        worldDir.resolve(WORLD_BEHAVIOR_PACKS_FILENAME), BehaviorPack.toJson(worldBehaviorPacks));

    var behaviorDir =
        behavior.makeDir(Files.createDirectory(worldDir.resolve(BEHAVIOR_PACKS_DIR_NAME)));

    var functionsDir = Files.createDirectory(behaviorDir.resolve(FUNCTIONS_DIR_NAME));
    Files.writeString(functionsDir.resolve(TICK_FILENAME), tickFunctions.toJson());
    FileUtil.createDirectoriesAndCopy(
        FIRST_LOAD_FUNCTION, functionsDir.resolve(FIRST_LOAD_FILENAME));
    Files.writeString(functionsDir.resolve(RELOAD_STRUCTURES_FILENAME), reloadStructuresFunction);

    var reloadDir = Files.createDirectory(functionsDir.resolve(RELOAD_DIR_NAME));
    for (var entry : structureFunctions.entrySet()) {
      FileUtil.createDirectoriesAndWriteString(
          reloadDir.resolve(entry.getKey().toPath(MCExtension.MCFUNCTION.toString())),
          entry.getValue());
    }

    return worldDir;
  }
}
