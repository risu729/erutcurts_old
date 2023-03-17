/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.time.Duration;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Caches {

  private final Duration DEFAULT_EXPIRE_DURATION = Duration.ofMinutes(3);

  @Contract(pure = true)
  public <K, V> @NotNull LoadingCache<K, V> newLoadingCache(
      @NotNull CacheLoader<? super K, V> loader) {
    return newDefaultCaffeine().build(loader);
  }

  @Contract(pure = true)
  public @NotNull Caffeine<Object, Object> newDefaultCaffeine() {
    return Caffeine.newBuilder()
        .scheduler(Scheduler.systemScheduler())
        .expireAfterAccess(DEFAULT_EXPIRE_DURATION);
  }
}
