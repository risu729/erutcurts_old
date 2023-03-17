/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "WeakerAccess"})
@UtilityClass
public class TypeAdapters {

  @SuppressWarnings("HardcodedFileSeparator")
  private final char DEFAULT_PATH_SEPARATOR = '/';

  @Contract(value = "_ -> new", pure = true)
  public <T> @NotNull TypeAdapter<T> createWithThrowable(
      @NotNull ThrowableFunction<? super String, ? extends T> reader) {
    return create(reader.toFunction());
  }

  @Contract(value = "_ -> new", pure = true)
  public <T> @NotNull TypeAdapter<T> create(@NotNull Function<? super String, ? extends T> reader) {
    return create(T::toString, reader);
  }

  @Contract(value = "_, _ -> new", pure = true)
  public <T> @NotNull TypeAdapter<T> createWithThrowable(
      @NotNull ThrowableFunction<? super T, String> writer,
      @NotNull ThrowableFunction<? super String, ? extends T> reader) {
    return create(writer.toFunction(), reader.toFunction());
  }

  @Contract(value = "_, _ -> new", pure = true)
  public <T> @NotNull TypeAdapter<T> create(
      @NotNull Function<? super T, String> writer,
      @NotNull Function<? super String, ? extends T> reader) {
    return new TypeAdapter<T>() {
      @Override
      public void write(@NotNull JsonWriter out, @NotNull T value) throws IOException {
        out.value(writer.apply(value));
      }

      @Override
      public T read(@NotNull JsonReader in) throws IOException {
        return reader.apply(in.nextString());
      }
    }.nullSafe();
  }

  @Contract(value = " -> new", pure = true)
  public @NotNull TypeAdapter<Path> newPathTypeAdapter() {
    return newPathTypeAdapter(DEFAULT_PATH_SEPARATOR);
  }

  @Contract(value = "_ -> new", pure = true)
  public @NotNull TypeAdapter<Path> newPathTypeAdapter(char separator) {
    return create(
        path ->
            StreamSupport.stream(path.spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining(String.valueOf(separator))),
        Path::of);
  }

  @SuppressWarnings("ProhibitedExceptionDeclared")
  @FunctionalInterface
  public interface ThrowableFunction<T, R> {

    @Contract(pure = true)
    static <T> @NotNull ThrowableFunction<T, T> identity() {
      return t -> t;
    }

    R apply(T t) throws Exception;

    @Contract(pure = true)
    default <V> @NotNull ThrowableFunction<V, R> compose(
        @NotNull ThrowableFunction<? super V, ? extends T> before) {
      return (V v) -> apply(before.apply(v));
    }

    @Contract(pure = true)
    default <V> @NotNull ThrowableFunction<T, V> andThen(
        @NotNull ThrowableFunction<? super R, ? extends V> after) {
      return (T t) -> after.apply(apply(t));
    }

    @SuppressWarnings("ProhibitedExceptionThrown")
    @Contract(pure = true)
    private @NotNull Function<T, R> toFunction() {
      return t -> {
        try {
          return apply(t);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };
    }
  }
}
