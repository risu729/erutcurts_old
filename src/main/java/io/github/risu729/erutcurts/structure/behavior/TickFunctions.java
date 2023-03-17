/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Joiner;
import com.google.common.io.MoreFiles;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.risu729.erutcurts.structure.MCExtension;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

record TickFunctions(@NotNull List<@NotNull Path> values) {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeHierarchyAdapter(
              Path.class,
              new TypeAdapter<Path>() {

                @SuppressWarnings("HardcodedFileSeparator")
                private static final Joiner JOINER = Joiner.on('/');

                @Override
                public void write(@NotNull JsonWriter jsonWriter, @NotNull Path path)
                    throws IOException {
                  var filename = MoreFiles.getNameWithoutExtension(path);
                  jsonWriter.value(
                      JOINER.join(
                          Optional.ofNullable(path.getParent())
                              .map(p -> p.resolve(filename))
                              .orElse(Path.of(filename))
                              .iterator()));
                }

                @Override
                public @NotNull Path read(@NotNull JsonReader jsonReader) throws IOException {
                  var path = Path.of(jsonReader.nextString());
                  var filename =
                      FileUtil.appendExtension(
                          path.getFileName(), MCExtension.MCFUNCTION.toString());
                  return Optional.ofNullable(path.getParent())
                      .map(p -> p.resolve(filename))
                      .orElse(filename);
                }
              }.nullSafe())
          .create();

  TickFunctions(@NotNull Path @NotNull ... values) {
    this(List.of(values));
  }

  TickFunctions {
    checkArgument(
        values.stream()
            .allMatch(path -> FileUtil.isExtension(path, MCExtension.MCFUNCTION.toString())));
  }

  @Contract(pure = true)
  @NotNull
  String toJson() {
    return GSON.toJson(this);
  }
}
