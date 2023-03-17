/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.nbt;

import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record Coordinate(int x, int y, int z) implements Comparable<Coordinate> {

  private static final Comparator<Coordinate> COMPARATOR =
      Comparator.comparingInt(Coordinate::x)
          .thenComparingInt(Coordinate::y)
          .thenComparingInt(Coordinate::z);

  public Coordinate(@NotNull List<@NotNull Integer> list) {
    this(list.get(0), list.get(1), list.get(2));
  }

  @Override
  public int compareTo(@NotNull Coordinate other) {
    return COMPARATOR.compare(this, other);
  }
}
