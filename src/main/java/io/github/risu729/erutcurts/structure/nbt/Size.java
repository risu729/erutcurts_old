/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.nbt;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public record Size(int x, int y, int z) {

  public Size(@NotNull List<@NotNull Integer> list) {
    this(list.get(0), list.get(1), list.get(2));
  }

  public Size {
    checkArgument(x > 0);
    checkArgument(y > 0);
    checkArgument(z > 0);
  }

  @Contract(pure = true)
  public int volume() {
    return x * y * z;
  }

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<@NotNull Coordinate> coordinates() {
    return IntStream.range(0, x)
        .boxed()
        .<Coordinate>mapMulti(
            (x2, consumer) ->
                IntStream.range(0, y)
                    .forEach(
                        y2 ->
                            IntStream.range(0, z)
                                .forEach(z2 -> consumer.accept(new Coordinate(x2, y2, z2)))))
        .toList();
  }
}
