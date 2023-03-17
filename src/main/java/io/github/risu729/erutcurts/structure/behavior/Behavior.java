/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkArgument;

import io.github.risu729.erutcurts.BotInfo;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.structure.MCExtension;
import io.github.risu729.erutcurts.structure.behavior.manifest.Manifest;
import io.github.risu729.erutcurts.structure.behavior.manifest.ManifestHeader;
import io.github.risu729.erutcurts.structure.behavior.manifest.ManifestMetadata;
import io.github.risu729.erutcurts.structure.behavior.manifest.ManifestModule;
import io.github.risu729.erutcurts.structure.nbt.Structure;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semver4j.Semver;

@Value
@Accessors(fluent = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Behavior {

  private static final Path MANIFEST_FILENAME = Path.of("manifest.json");
  private static final Path PACK_ICON_FILENAME = Path.of("pack_icon.png");
  private static final Path STRUCTURES_DIR_NAME = Path.of("structures");
  private static final Path METADATA_FILENAME = Path.of("metadata.json");

  private static final Path DEFAULT_PACK_ICON =
      Erutcurts.RESOURCES_DIR.resolve("default_pack_icon.png");

  @NotNull String packName;
  @NotNull Path packIcon;
  @NotNull Manifest manifest;
  @NotNull List<@NotNull StructureMetadata> structureMetadata;
  @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures;

  @SuppressWarnings("HardcodedLineSeparator")
  public Behavior(
      @Nullable String packName,
      @Nullable Path packIcon,
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures) {

    checkArgument(!structures.isEmpty(), "structures must not be empty");

    this.packName =
        packName == null
            ? structures.keySet().stream()
                .findFirst()
                .map(Identifier::toStringWithoutDefaultNamespace)
                .orElseThrow()
            : packName;
    this.packIcon = packIcon == null ? DEFAULT_PACK_ICON : packIcon;
    this.structures = Map.copyOf(structures);

    Map<Identifier, Structure> nbtStructures = new HashMap<>();
    for (var entry : structures.entrySet()) {
      nbtStructures.put(entry.getKey(), Structure.fromNbt(entry.getValue()));
    }

    this.structureMetadata =
        nbtStructures.entrySet().stream()
            .map(entry -> new StructureMetadata(entry.getKey(), entry.getValue()))
            .toList();

    var description =
        "Structures: %s\n*Generated with %s"
            .formatted(
                structureMetadata.stream()
                    .map(StructureMetadata::identifier)
                    .map(Identifier::toString)
                    .collect(Collectors.joining(", ")),
                BotInfo.NAME);

    var minEngineVersion =
        structureMetadata.stream()
            .map(StructureMetadata::minEngineVersion)
            .max(Comparator.naturalOrder())
            .orElse(ManifestHeader.LOWEST_GAME_VERSION);

    this.manifest =
        Manifest.builder()
            .header(
                ManifestHeader.builder()
                    .type(ManifestModule.Type.DATA)
                    .name("Structures: %s".formatted(this.packName))
                    .description(description)
                    .minEngineVersion(minEngineVersion)
                    .build())
            .module(ManifestModule.builder().type(ManifestModule.Type.DATA).build())
            .metadata(
                ManifestMetadata.builder()
                    .addGeneratedWith(
                        ManifestMetadata.GeneratedWith.builder()
                            .name(BotInfo.NAME)
                            .version(Semver.parse(BotInfo.VERSION))
                            .build())
                    .build())
            .build();
  }

  @CheckReturnValue
  public static @NotNull CloseablePath generate(
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures) throws IOException {
    return generate(null, null, structures);
  }

  @CheckReturnValue
  public static @NotNull CloseablePath generate(
      @Nullable String packName,
      @Nullable Path packIcon,
      @NotNull Map<@NotNull Identifier, ? extends @NotNull Path> structures)
      throws IOException {
    var tempDir = FileUtil.createTempDir();
    try (var closeablePackDir =
        CloseablePath.of(new Behavior(packName, packIcon, structures).makeDir(tempDir))) {
      var packDir = closeablePackDir.path();
      return CloseablePath.of(
          FileUtil.zip(
              tempDir.resolve(
                  FileUtil.appendExtension(packDir.getFileName(), MCExtension.MCPACK.toString())),
              packDir,
              false),
          true);
    }
  }

  @CheckReturnValue
  @NotNull
  Path makeDir(@NotNull Path parent) throws IOException {
    var packDir = Files.createDirectory(parent.resolve(packName));
    Files.writeString(packDir.resolve(MANIFEST_FILENAME), manifest.toJson());
    Files.copy(packIcon, packDir.resolve(PACK_ICON_FILENAME));
    Files.writeString(
        packDir.resolve(METADATA_FILENAME), StructureMetadata.toJson(structureMetadata));
    var structuresDir = Files.createDirectory(packDir.resolve(STRUCTURES_DIR_NAME));
    for (var entry : structures.entrySet()) {
      FileUtil.createDirectoriesAndCopy(
          entry.getValue(), structuresDir.resolve(entry.getKey().toPath()));
    }
    return packDir;
  }
}
