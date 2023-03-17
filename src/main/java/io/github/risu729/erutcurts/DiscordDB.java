/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.github.risu729.erutcurts.util.Caches;
import io.github.risu729.erutcurts.util.Forums;
import io.github.risu729.erutcurts.util.gson.SemverTypeAdapter;
import io.github.risu729.erutcurts.util.gson.TypeAdapters;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.FileProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

// each database is stored in a forum post in the forum channel as a json file
// all members are static for easy lazy initialization
@UtilityClass
public class DiscordDB {

  private final int DATABASE_HISTORY_LIMIT = 5;

  // all used TypeAdapter must be registered here
  private final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .serializeNulls()
          // Settings
          .registerTypeAdapter(
              DiscordLocale.class,
              TypeAdapters.create(DiscordLocale::getLocale, DiscordLocale::from))
          // LevelVersions
          .registerTypeAdapter(Semver.class, SemverTypeAdapter.newInstance(true, semver -> false))
          .create();

  // key is the name of data with type of data
  // value should be a mutable collection, if not the bot sends all changes so this is meaningless
  private final Cache<String, Object> CACHE =
      Caches.newDefaultCaffeine()
          .<String, Object>removalListener(
              (key, value, removalCause) -> {
                checkState(
                    removalCause != RemovalCause.COLLECTED); // weak or soft reference is not used
                if (removalCause == RemovalCause.REPLACED) {
                  return; // don't send when replaced to new value
                }
                checkNotNull(key);
                getDatabaseChannel(key)
                    .sendFiles(FileUpload.fromData(GSON.toJson(value).getBytes(), key + ".json"))
                    .queue();
              })
          .build();

  @SuppressWarnings({"ParameterNameDiffersFromOverriddenParameter", "unchecked"})
  @CheckReturnValue
  public <T> @NotNull T get(
      @NotNull String name,
      @NotNull TypeToken<T> type,
      @NotNull UnaryOperator<? super T> formatter,
      @NotNull Supplier<? extends T> fallback) {
    return (T) CACHE.get(name, key -> readData(name, type).map(formatter).orElseGet(fallback));
  }

  @SuppressWarnings({"ParameterNameDiffersFromOverriddenParameter", "unchecked"})
  @CheckReturnValue
  public <T> @NotNull T get(
      @NotNull String name,
      @NotNull TypeToken<? super T> type,
      @NotNull Supplier<? extends T> fallback) {
    return (T) CACHE.get(name, key -> readData(name, type).orElseGet(fallback));
  }

  @SuppressWarnings({"ParameterNameDiffersFromOverriddenParameter", "unchecked"})
  @CheckReturnValue
  public <T> @NotNull Optional<T> get(@NotNull String name, @NotNull TypeToken<T> type) {
    return Optional.ofNullable((T) CACHE.get(name, key -> readData(name, type).orElse(null)));
  }

  @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
  public <T> void put(@NotNull String name, @NotNull T value) {
    CACHE.put(name, value);
  }

  void shutdown() {
    // when invalidated, removalListener sends to Discord
    CACHE.invalidateAll();
  }

  @CheckReturnValue
  private @NotNull ThreadChannel getDatabaseChannel(@NotNull String name) {
    var forum =
        checkNotNull(
            Erutcurts.getJDA()
                .getChannelById(ForumChannel.class, Envs.getEnv("DATABASE_CHANNEL_ID")));
    return Forums.getForumPost(forum, name)
        .orElseGet(() -> Forums.createEmptyForumPost(forum, name));
  }

  @CheckReturnValue
  private <T> @NotNull Optional<T> readData(@NotNull String name, @NotNull TypeToken<T> type) {
    return getDatabaseChannel(name).getIterableHistory().stream()
        .filter(Erutcurts::isSelfMessage) // not allowed to overwrite by user
        .limit(DATABASE_HISTORY_LIMIT)
        .map(Message::getAttachments)
        .filter(list -> list.size() == 1) // exclude if there is no attachments or more than one
        .flatMap(List::stream)
        .filter(attachment -> attachment.getFileName().equals(name + ".json"))
        .map(Message.Attachment::getProxy)
        .map(FileProxy::download)
        .<Optional<T>>map(
            future -> {
              try (var inputStream = future.join();
                  var reader = new InputStreamReader(inputStream)) {
                return Optional.of(GSON.fromJson(reader, type.getType()));
              } catch (JsonSyntaxException ignored) {
                // ignore if the file is not a valid json
                return Optional.empty();
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            })
        .flatMap(Optional::stream)
        .findFirst();
  }
}
