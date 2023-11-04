/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.localization;

import io.github.risu729.erutcurts.BotInfo;
import io.github.risu729.erutcurts.Erutcurts;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.jetbrains.annotations.NotNull;

@Value
@AllArgsConstructor(staticName = "of", access = AccessLevel.PACKAGE)
class FormattableLocalizationFunction implements LocalizationFunction {

  @SuppressWarnings("AccessOfSystemProperties")
  private static final Map<String, String> VARIABLES =
      Map.of(
          "NAME",
          BotInfo.NAME,
          "VERSION",
          BotInfo.VERSION,
          "DEVELOPER",
          BotInfo.DEVELOPER,
          "SUPPORTED_LANGUAGES",
          BotInfo.SUPPORTED_LOCALES.toString(),
          "JAVA_VERSION",
          System.getProperty("java.version"),
          "JAVA_VENDOR",
          System.getProperty("java.vendor"),
          "SERVER_OS",
          System.getProperty("os.name") + System.getProperty("os.version"),
          "JDA_VERSION",
          JDAInfo.VERSION,
          "BEDROCK_SAMPLES_VERSION",
          BotInfo.BEDROCK_SAMPLES_VERSION,
          "START_TIME",
          Erutcurts.START_TIME.toString());

  @NotNull LocalizationFunction localizationFunction;

  @SuppressWarnings("ReassignedVariable")
  @Override
  public @NotNull Map<DiscordLocale, String> apply(@NotNull String localizationKey) {
    var map = localizationFunction.apply(localizationKey);
    map.replaceAll(
        (locale, value) -> {
          for (var entry : VARIABLES.entrySet()) {
            value = value.replace("$" + entry.getKey() + "$", entry.getValue());
          }
          return value;
        });
    return map;
  }
}
