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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// reference: https://github.com/google/gson/issues/512#issuecomment-1203356412
@NoArgsConstructor(staticName = "newInstance")
public final class EmptyCollectionTypeAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> @Nullable TypeAdapter<T> create(@NotNull Gson gson, @NotNull TypeToken<T> type) {

    var rawType = type.getRawType();
    var isMap = Map.class.isAssignableFrom(rawType);

    if (!Collection.class.isAssignableFrom(rawType) && !isMap) {
      return null;
    }

    var delegate = gson.getDelegateAdapter(this, type);

    return new TypeAdapter<T>() {
      @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
      @Override
      public void write(@NotNull JsonWriter writer, @NotNull T value) throws IOException {
        var isEmpty = isMap ? ((Map<?, ?>) value).isEmpty() : ((Collection<?>) value).isEmpty();
        delegate.write(writer, isEmpty ? null : value);
      }

      // do not deserialize empty collections as null
      // because Gson always returns null if the value is not present and this can't be changed
      @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
      @Override
      public T read(@NotNull JsonReader reader) throws IOException {
        return delegate.read(reader);
      }
    }.nullSafe();
  }
}
