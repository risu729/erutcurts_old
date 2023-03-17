/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Streams;
import io.github.risu729.erutcurts.util.interaction.ExecutableOptionData;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class CommandOptionUtil {

  private final String DUMMY_DESCRIPTION = "---"; // zero-width space

  @Contract(pure = true)
  public @NotNull OptionData createWithBlankDescription(
      @NotNull OptionType type, @NotNull String name) {
    return new OptionData(type, name, DUMMY_DESCRIPTION);
  }

  @Contract(pure = true)
  public @NotNull List<@NotNull OptionData> createVarArgs(
      @NotNull OptionData option, int requiredCount, int optionalCount) {
    checkArgument(requiredCount >= 0);
    checkArgument(optionalCount >= 0);
    checkArgument(requiredCount + optionalCount > 0);
    checkArgument(requiredCount + optionalCount <= OptionData.MAX_CHOICES);

    Supplier<OptionData> copySupplier;
    if (option instanceof ExecutableOptionData executableOption) {
      copySupplier = executableOption::clone;
    } else {
      var data = option.toData();
      copySupplier = () -> OptionData.fromData(data);
    }

    return Streams.mapWithIndex(
            Stream.generate(copySupplier),
            (optionData, index) ->
                optionData
                    .setName(option.getName() + (index + 1))
                    .setRequired(index + 1 <= requiredCount))
        .limit(requiredCount + optionalCount)
        .toList();
  }
}
