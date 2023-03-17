/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(staticName = "newInstance")
public final class SnakeCaseEnumTypeAdapterFactory implements TypeAdapterFactory {

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> @Nullable TypeAdapter<T> create(@NotNull Gson gson, @NotNull TypeToken<T> typeToken) {

    // returns Class<? super T> so cast is needed
    var rawType = (Class<T>) typeToken.getRawType();

    // ignores if not Enum
    if (!rawType.isEnum()) {
      return null;
    }

    List<T> enumConstants = List.of(rawType.getEnumConstants());

    Map<T, SerializedName> serializedNameMap =
        enumConstants.stream()
            .<Map.Entry<T, SerializedName>>mapMulti(
                (constant, consumer) -> {
                  try {
                    Optional.ofNullable(
                            rawType
                                .getField(((Enum) constant).name())
                                .getAnnotation(SerializedName.class))
                        .ifPresent(
                            serializedName -> consumer.accept(Map.entry(constant, serializedName)));
                  } catch (NoSuchFieldException e) {
                    // it must exist because the name is from enum constant
                    throw new AssertionError(e);
                  }
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<T, String> constantToName =
        enumConstants.stream()
            .collect(
                Collectors.toMap(
                    UnaryOperator.identity(),
                    constant -> ((Enum) constant).name().toLowerCase(Locale.ENGLISH)));

    // create maps for performance
    Map<T, String> constantToJson =
        enumConstants.stream()
            .collect(
                Collectors.toMap(
                    UnaryOperator.identity(),
                    constant ->
                        Optional.ofNullable(serializedNameMap.get(constant))
                            .map(SerializedName::value)
                            .orElseGet(() -> constantToName.get(constant))));

    Map<String, T> jsonToConstant = new HashMap<>();
    for (var constant : enumConstants) {
      jsonToConstant.put(constantToName.get(constant), constant);
      Optional.ofNullable(serializedNameMap.get(constant))
          .ifPresent(
              serializedName -> {
                jsonToConstant.put(serializedName.value(), constant);
                Arrays.stream(serializedName.alternate())
                    .forEach(alternate -> jsonToConstant.put(alternate, constant));
              });
    }

    return new TypeAdapter<T>() {
      public void write(@NotNull JsonWriter jsonWriter, @NotNull T value) throws IOException {
        jsonWriter.value(constantToJson.get(value));
      }

      public @NotNull T read(@NotNull JsonReader jsonReader) throws IOException {
        return jsonToConstant.get(jsonReader.nextString());
      }
    }.nullSafe();
  }
}
