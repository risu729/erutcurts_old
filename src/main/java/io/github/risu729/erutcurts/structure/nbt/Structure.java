/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.nbt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Streams;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import nl.itslars.mcpenbt.NBTUtil;
import nl.itslars.mcpenbt.tags.CompoundTag;
import nl.itslars.mcpenbt.tags.IntTag;
import nl.itslars.mcpenbt.tags.ListTag;
import nl.itslars.mcpenbt.tags.Tag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

// based on "Bedrock mcstructure file format" by tryashtar
// https://gist.github.com/tryashtar/87ad9654305e5df686acab05cc4b6205

public record Structure(
    int formatVersion,
    @NotNull Size size,
    @NotNull List<@NotNull List<@NotNull List<@NotNull Layers>>> blockIndices,
    @NotNull List<@NotNull CompoundTag> entities,
    @NotNull Coordinate structureWorldOrigin) {

  public Structure {
    checkArgument(formatVersion > 0);
  }

  @Contract("_ -> new")
  @CheckReturnValue
  public static @NotNull Structure fromNbt(@NotNull Path path) {
    return fromNbt(NBTUtil.read(false, path).getAsCompound());
  }

  @Contract("_ -> new")
  @CheckReturnValue
  private static @NotNull Structure fromNbt(@NotNull CompoundTag root) {

    var structure = root.getByName("structure").map(Tag::getAsCompound).orElseThrow();

    var defaultPalette =
        structure
            .getByName("palette")
            .map(Tag::getAsCompound)
            .flatMap(tag -> tag.getByName("default"))
            .map(Tag::getAsCompound)
            .orElseThrow();
    List<Block> basicBlockPalette =
        defaultPalette
            .getByName("block_palette")
            .map(Tag::<CompoundTag>getAsList)
            .map(ListTag::getElements)
            .stream()
            .flatMap(List::stream)
            .map(
                block ->
                    new Block(
                        block.getByName("name").map(Tag::getAsString).orElseThrow(),
                        block.getByName("states").map(Tag::getAsCompound).orElseThrow(),
                        block.getByName("version").map(Tag::getAsInt).orElseThrow()))
            .toList();
    Optional<CompoundTag> blockPositionData =
        defaultPalette.getByName("block_position_data").map(Tag::getAsCompound);

    List<Block> flatBlockIndices =
        Streams.mapWithIndex(
                structure
                    .getByName("block_indices")
                    .map(Tag::<ListTag<IntTag>>getAsList)
                    .map(ListTag::getElements)
                    .stream()
                    .flatMap(List::stream)
                    .map(ListTag::getElements)
                    .flatMap(List::stream)
                    .mapToInt(IntTag::getValue)
                    .mapToObj(value -> value == -1 ? Block.VOID : basicBlockPalette.get(value)),

                // add blockPositionData if exists
                (block, index) ->
                    blockPositionData
                        .flatMap(data -> data.getByName(String.valueOf(index)))
                        .map(Tag::getAsCompound)
                        // null check for block is not needed because void never has
                        // blockPositionData
                        .map(
                            positionData ->
                                new Block(
                                    block,
                                    positionData
                                        .getByName("block_entity_data")
                                        .map(Tag::getAsCompound)
                                        .orElse(null),
                                    positionData
                                        .getByName("tick_queue_data")
                                        .map(Tag::<CompoundTag>getAsList)
                                        .map(ListTag::getElements)
                                        .stream()
                                        .flatMap(List::stream)
                                        .map(tickQueueData -> tickQueueData.getByName("tick_delay"))
                                        .flatMap(Optional::stream)
                                        .map(Tag::getAsInt)
                                        .toList()))
                        .orElse(block))
            .toList();

    var size =
        new Size(
            root.getByName("size").map(Tag::<IntTag>getAsList).map(ListTag::getElements).stream()
                .flatMap(List::stream)
                .map(IntTag::getValue)
                .toList());
    checkState(flatBlockIndices.size() == size.volume() * 2);

    List<List<List<Layers>>> blockIndices = new ArrayList<>(size.x());
    for (var i = 0; i < size.x(); i++) {
      blockIndices.add(new ArrayList<>(size.y()));
      for (var j = 0; j < size.y(); j++) {
        blockIndices.get(i).add(new ArrayList<>(size.z()));
        for (var k = 0; k < size.z(); k++) {
          var index = i * size.y() * size.z() + j * size.z() + k;
          blockIndices
              .get(i)
              .get(j)
              .add(
                  new Layers(
                      flatBlockIndices.get(index), flatBlockIndices.get(index + size.volume())));
        }
      }
    }

    return new Structure(
        root.getByName("format_version").map(Tag::getAsInt).map(IntTag::getValue).orElseThrow(),
        size,
        blockIndices,
        structure
            .getByName("entities")
            .map(Tag::<CompoundTag>getAsList)
            .map(ListTag::getElements)
            .orElse(Collections.emptyList()),
        new Coordinate(
            root
                .getByName("structure_world_origin")
                .map(Tag::<IntTag>getAsList)
                .map(ListTag::getElements)
                .stream()
                .flatMap(List::stream)
                .map(IntTag::getValue)
                .toList()));
  }

  // get the greatest version of the blocks
  @Contract(pure = true)
  public @NotNull Semver getMinEngineVersion() {
    return blockIndices.stream()
        .flatMap(List::stream)
        .flatMap(List::stream)
        .filter(Predicate.not(Layers::isVoid))
        .<Block>mapMulti(
            (layer, consumer) -> {
              consumer.accept(layer.primary());
              consumer.accept(layer.secondary());
            })
        .filter(Objects::nonNull)
        .map(Block::version)
        .distinct()
        .map(version -> version.substring(0, version.lastIndexOf('.')))
        .map(Semver::parse)
        .max(Comparator.naturalOrder())
        .orElseThrow();
  }
}
