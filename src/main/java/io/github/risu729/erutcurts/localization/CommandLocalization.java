/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.localization;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class CommandLocalization {

  private final LocalizationFunction LOCALIZATION_FUNCTION =
      Localizations.createLocalizationFunction("SlashCommands");

  private final String DUMMY_DESCRIPTION = "\u200B"; // Zero-width space

  @Contract("_ -> new")
  @CheckReturnValue
  public @NotNull SlashCommandData createSlashCommand(@NotNull String name) {
    return Commands.slash(name, DUMMY_DESCRIPTION).setLocalizationFunction(LOCALIZATION_FUNCTION);
  }

  @Contract("_ -> new")
  @CheckReturnValue
  public @NotNull SubcommandGroupData createSubcommandGroup(@NotNull String name) {
    return new SubcommandGroupData(name, DUMMY_DESCRIPTION);
  }

  @Contract("_ -> new")
  @CheckReturnValue
  public @NotNull SubcommandData createSubcommand(@NotNull String name) {
    return new SubcommandData(name, DUMMY_DESCRIPTION);
  }

  @Contract("_, _ -> new")
  @CheckReturnValue
  public @NotNull OptionData createOption(@NotNull OptionType type, @NotNull String name) {
    return new OptionData(type, name, DUMMY_DESCRIPTION);
  }

  @Contract("_, _ -> new")
  @CheckReturnValue
  public @NotNull List<@NotNull OptionData> createVarArgsOption(
      @NotNull OptionData option, int count) {
    DataObject data = option.toData();
    return Stream.concat(
            Stream.of(option),
            Streams.mapWithIndex(
                Stream.generate(() -> OptionData.fromData(data)),
                (optionData, index) ->
                    optionData.setName(option.getName() + (index + 2)).setRequired(false)))
        .limit(count)
        .toList();
  }

  @Contract(" _ -> new")
  @CheckReturnValue
  @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
  public @NotNull Command.Choice createChoice(@NotNull String name) {
    return createChoice(name, name);
  }

  @Contract("_, _ -> new")
  @CheckReturnValue
  public @NotNull Command.Choice createChoice(@NotNull String name, @NotNull String value) {
    return new Command.Choice(name, value);
  }
}
