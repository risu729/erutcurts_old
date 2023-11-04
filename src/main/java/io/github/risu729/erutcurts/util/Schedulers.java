/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import com.google.common.util.concurrent.MoreExecutors;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Schedulers {

  @Contract(pure = true)
  public @NotNull ScheduledExecutorService newImmediateExitingScheduler() {
    var threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    threadPoolExecutor.setRemoveOnCancelPolicy(true);
    return MoreExecutors.getExitingScheduledExecutorService(threadPoolExecutor, Duration.ZERO);
  }
}
