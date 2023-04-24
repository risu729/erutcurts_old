/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.nbt;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import nl.itslars.mcpenbt.tags.CompoundTag;
import nl.itslars.mcpenbt.tags.IntTag;
import nl.itslars.mcpenbt.tags.StringTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Block(
    @NotNull String name,
    @NotNull CompoundTag states,
    // version contains 4 integers like 1.19.60.24 so can't use Semver as type
    @NotNull String version,
    @Nullable CompoundTag blockEntityData,
    @NotNull List<@NotNull Integer> tickDelays) {

  @SuppressWarnings("WeakerAccess")
  public static final Block VOID = null;

  private static final Pattern VERSION_PATTERN = Pattern.compile("^(?:\\d+\\.){3}\\d+$");

  public Block(@NotNull StringTag name, @NotNull CompoundTag states, @NotNull IntTag version) {
    this(
        name.getValue(),
        states,
        Bytes.asList(Ints.toByteArray(version.getValue())).stream()
            .mapToInt(Byte::toUnsignedInt)
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(".")),
        null,
        List.of());
  }

  public Block(
      @NotNull Block basicBlock,
      @Nullable CompoundTag blockEntityData,
      @NotNull List<? extends @NotNull IntTag> tickDelays) {
    this(
        basicBlock.name(),
        basicBlock.states(),
        basicBlock.version(),
        blockEntityData,
        tickDelays.stream().map(IntTag::getValue).toList());
  }

  public Block {
    checkArgument(!name.isBlank());
    checkArgument(blockEntityData == null || !blockEntityData.getElements().isEmpty());
    checkArgument(tickDelays.stream().allMatch(delay -> delay >= 0));
    checkArgument(!version.isBlank() && VERSION_PATTERN.matcher(version).matches());
  }
}
