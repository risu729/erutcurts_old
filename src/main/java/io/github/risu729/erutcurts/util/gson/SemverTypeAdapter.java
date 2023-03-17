/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.gson;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.semver4j.Semver;

@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public final class SemverTypeAdapter extends TypeAdapter<Semver> {

  boolean isPrettyPrinting;
  @NotNull Predicate<? super Semver> intArrayPredicate;

  @Contract("_, _ -> new")
  @CheckReturnValue
  public static @NotNull TypeAdapter<Semver> newInstance(
      boolean isPrettyPrinting, @NotNull Predicate<? super Semver> intArrayPredicate) {
    return new SemverTypeAdapter(isPrettyPrinting, intArrayPredicate).nullSafe();
  }

  @Override
  public void write(@NotNull JsonWriter jsonWriter, @NotNull Semver semver) throws IOException {
    // if intArrayPredicate is true, write as an array
    if (intArrayPredicate.test(semver)) {
      jsonWriter.beginArray();
      // write the int array in one line even if pretty printing is enabled
      if (isPrettyPrinting) {
        jsonWriter.setIndent("");
      }
      jsonWriter
          .value(semver.getMajor())
          .value(semver.getMinor())
          .value(semver.getPatch())
          .endArray();
      if (isPrettyPrinting) {
        jsonWriter.setIndent(GsonConstant.PRETTY_PRINTING_INDENT);
      }
    } else {
      // if false, write as a string
      jsonWriter.value(semver.getVersion());
    }
  }

  @Override
  public Semver read(@NotNull JsonReader jsonReader) throws IOException {
    String semver;
    if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
      jsonReader.beginArray();
      List<Integer> array = new ArrayList<>();
      while (jsonReader.hasNext()) {
        array.add(jsonReader.nextInt());
      }
      jsonReader.endArray();
      semver = array.stream().map(String::valueOf).collect(Collectors.joining("."));
    } else {
      semver = jsonReader.nextString();
    }
    return checkNotNull(Semver.parse(semver));
  }
}
