/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.localization;

import com.google.common.base.Joiner;
import io.github.risu729.erutcurts.BotInfo;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class MessageLocalization {

  private final String MESSAGES_KEY_PREFIX = "messages";
  private final String EMBEDS_KEY_PREFIX = "embeds";
  private final String FIELDS_KEY_PREFIX = "fields";

  private final LocalizationFunction LOCALIZATION_FUNCTION =
      Localizations.createLocalizationFunction("Messages");

  private final Joiner KEY_JOINER = Joiner.on('.');

  private @NotNull Optional<String> getValue(@NotNull String key, @NotNull DiscordLocale locale) {
    return Optional.ofNullable(LOCALIZATION_FUNCTION.apply(key).get(locale));
  }

  @Contract("_, _ -> new")
  @CheckReturnValue
  public @NotNull MessageCreateBuilder createMessageBuilder(
      @NotNull DiscordLocale locale, @NotNull String key) {
    return new MessageCreateBuilder()
        .setContent(
            getValue(KEY_JOINER.join(MESSAGES_KEY_PREFIX, key, "content"), locale).orElseThrow());
  }

  @Contract("_, _ -> new")
  @CheckReturnValue
  public @NotNull EmbedBuilder createEmbedBuilder(
      @NotNull DiscordLocale locale, @NotNull String key) {
    return new EmbedBuilder()
        .setTitle(getValue(KEY_JOINER.join(EMBEDS_KEY_PREFIX, key, "title"), locale).orElseThrow())
        .setDescription(
            getValue(KEY_JOINER.join(EMBEDS_KEY_PREFIX, key, "description"), locale).orElse(null))
        .setColor(BotInfo.THEME_COLOR);
  }

  @Contract("_, _, _, _ -> new")
  @CheckReturnValue
  public @NotNull MessageEmbed.Field createField(
      @NotNull DiscordLocale locale,
      @NotNull String embedKey,
      @NotNull String fieldKey,
      boolean inline) {
    var key = KEY_JOINER.join(EMBEDS_KEY_PREFIX, embedKey, FIELDS_KEY_PREFIX, fieldKey);
    return new MessageEmbed.Field(
        getValue(KEY_JOINER.join(key, "name"), locale).orElseThrow(),
        getValue(KEY_JOINER.join(key, "value"), locale).orElse(null),
        inline);
  }

  @Contract("_, _, _, _ -> new")
  @CheckReturnValue
  public @NotNull List<MessageEmbed.@NotNull Field> createFields(
      @NotNull DiscordLocale locale,
      @NotNull String embedKey,
      @NotNull List<@NotNull String> fieldKeys,
      boolean inline) {
    return fieldKeys.stream().map(key -> createField(locale, embedKey, key, inline)).toList();
  }
}
