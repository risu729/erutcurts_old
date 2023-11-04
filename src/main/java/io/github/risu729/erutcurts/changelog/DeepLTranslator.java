/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.changelog;

import com.deepl.api.DeepLException;
import com.deepl.api.Language;
import com.deepl.api.Translator;
import io.github.risu729.erutcurts.Envs;
import io.github.risu729.erutcurts.misc.Notifications;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

@SuppressWarnings("ProhibitedExceptionThrown")
public final class DeepLTranslator {

  static final Translator TRANSLATOR = new Translator(Envs.getEnv("DEEPL_AUTH_KEY"));
  static final Map<DiscordLocale, Language> DISCORD_LOCALE_TARGET_LANGUAGE_MAP =
      createDiscordLocaleTargetLanguageMap();

  static {
    if (!Translator.isFreeAccountAuthKey(Envs.getEnv("DEEPL_AUTH_KEY"))) {
      Notifications.sendNotification(
          "DeepL API key is not a free key. Translation may incur costs.");
    }
  }

  @Contract(" -> fail")
  private DeepLTranslator() {
    throw new AssertionError();
  }

  static @NotNull MessageCreateData translateMessage(
      @NotNull Message message,
      @NotNull DiscordLocale sourceLocale,
      @NotNull DiscordLocale targetLocale) {
    Language source = DISCORD_LOCALE_TARGET_LANGUAGE_MAP.get(sourceLocale);
    Language target = DISCORD_LOCALE_TARGET_LANGUAGE_MAP.get(targetLocale);
    // TODO: support embeds translations
    // TODO: escape markdown, custom emojis, and unicode emojis
    // TODO: detect source language
    try {
      return MessageCreateBuilder.fromMessage(message)
          .setContent(
              TRANSLATOR
                  .translateText(message.getContentStripped(), source.getCode(), target.getCode())
                  .getText())
          .build();
    } catch (DeepLException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  // if a DiscordLocale is not contained, it is not supported by DeepL
  private static @NotNull @UnmodifiableView Map<@NotNull DiscordLocale, @NotNull Language>
      createDiscordLocaleTargetLanguageMap() {
    List<Language> targetLanguages;
    try {
      targetLanguages = TRANSLATOR.getTargetLanguages();
    } catch (DeepLException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    Map<Locale, Language> deeplLanguages =
        targetLanguages.stream()
            .collect(
                Collectors.toMap(
                    language -> Locale.forLanguageTag(language.getCode()),
                    UnaryOperator.identity()));
    Map<DiscordLocale, Language> map = new EnumMap<>(DiscordLocale.class);
    for (var discordLocale : DiscordLocale.values()) {
      var locale = Locale.forLanguageTag(discordLocale.getLocale());
      // search for an exact match first and then for a match only for the language
      Optional.ofNullable(deeplLanguages.get(locale))
          .or(
              () ->
                  Optional.ofNullable(
                      deeplLanguages.get(Locale.forLanguageTag(locale.getLanguage()))))
          .ifPresent(language -> map.put(discordLocale, language));
    }
    return Collections.unmodifiableMap(map);
  }
}
