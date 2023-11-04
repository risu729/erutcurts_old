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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class Localizations {

  private static final ClassLoader CLASS_LOADER = newResourceBundleClassLoader();

  @Contract(" -> fail")
  private Localizations() {
    throw new AssertionError();
  }

  // use DEFAULT_LOCALE for unknown languages
  @Contract("_ -> new")
  @CheckReturnValue
  static @NotNull LocalizationFunction createLocalizationFunction(@NotNull String baseName) {
    Map<DiscordLocale, ResourceBundle> bundles =
        BotInfo.SUPPORTED_LOCALES.stream()
            .map(DiscordLocale::getLocale)
            .map(Locale::forLanguageTag)
            .map(locale -> ResourceBundle.getBundle(baseName, locale, CLASS_LOADER))
            .collect(
                Collectors.toMap(
                    bundle -> DiscordLocale.from(bundle.getLocale()), UnaryOperator.identity()));
    return Arrays.stream(DiscordLocale.values())
        .filter(Predicate.not(DiscordLocale.UNKNOWN::equals)) // use of unknown causes exception
        .collect(
            Collector.of(
                ResourceBundleLocalizationFunction::empty,
                (builder, locale) ->
                    builder.addBundle(
                        bundles.getOrDefault(locale, bundles.get(BotInfo.DEFAULT_LOCALE)), locale),
                (e1, e2) -> e1,
                builder -> FormattableLocalizationFunction.of(builder.build())));
  }

  @SuppressWarnings({"ProhibitedExceptionThrown", "ClassLoaderInstantiation"})
  @Contract(" -> new")
  @CheckReturnValue
  private static @NotNull ClassLoader newResourceBundleClassLoader() {
    try {
      return new URLClassLoader(
          new URL[] {Erutcurts.RESOURCES_DIR.resolve("texts").toUri().toURL()});
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
