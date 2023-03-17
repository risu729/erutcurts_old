/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.risu729.erutcurts.util.gson.TypeAdapters;
import java.awt.Color;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"EmptyClass", "WeakerAccess"})
@UtilityClass
public class BotInfo {

  public final String NAME = "Erutcurts";
  public final String VERSION = "1.0.0";
  public final String DEVELOPER = "Risu (@risu729)";
  public final String GITHUB_URL = "https://github.com/risu729/erutcurts";
  public final Color THEME_COLOR = new Color(191, 148, 228);
  public final DiscordLocale DEFAULT_LOCALE = DiscordLocale.JAPANESE;
  public final Set<DiscordLocale> SUPPORTED_LOCALES =
      Collections.unmodifiableSet(EnumSet.of(DEFAULT_LOCALE));
  public final String BEDROCK_SAMPLES_VERSION;

  private final Path SAMPLES_VERSION_PATH =
      Erutcurts.RESOURCES_DIR.resolve(Path.of("bedrock-samples", "version.json"));

  static {
    try {
      var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
      BEDROCK_SAMPLES_VERSION =
          new GsonBuilder()
              .registerTypeAdapter(
                  LocalDate.class,
                  TypeAdapters.create(formatter::format, s -> LocalDate.parse(s, formatter)))
              .create()
              .<Map<String, VersionData>>fromJson(
                  Files.readString(SAMPLES_VERSION_PATH),
                  new TypeToken<Map<String, VersionData>>() {}.getType())
              .get("latest")
              .version();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private record VersionData(@NotNull String version, @NotNull LocalDate date) {}
}
