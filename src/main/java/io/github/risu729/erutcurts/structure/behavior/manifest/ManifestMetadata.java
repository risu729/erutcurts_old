/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.risu729.erutcurts.util.gson.GsonConstant;
import io.github.risu729.erutcurts.util.gson.SemverTypeAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.semver4j.Semver;

@SuppressWarnings("unused")
@Builder(toBuilder = true)
@With
public record ManifestMetadata(
    @Singular @Nullable List<@NotNull String> authors,
    @Nullable URL url,
    @Nullable String license,
    @JsonAdapter(GeneratedWithTypeAdapter.class) @Singular("addGeneratedWith") @Nullable
        List<@NotNull GeneratedWith> generatedWith) {

  public ManifestMetadata {
    checkArgument(authors == null || authors.stream().noneMatch(String::isBlank));
    checkArgument(license == null || !license.isBlank());
  }

  @Contract(pure = true)
  public @NotNull List<@NotNull String> authors() {
    return authors == null ? List.of() : authors;
  }

  @Contract(pure = true)
  public @NotNull List<@NotNull GeneratedWith> generatedWith() {
    return generatedWith == null ? List.of() : generatedWith;
  }

  @Builder(toBuilder = true)
  @With
  public record GeneratedWith(
      @NotNull String name, @Singular @NotNull List<@NotNull Semver> versions) {

    public GeneratedWith {
      checkArgument(!name.isBlank());
      checkArgument(!versions.isEmpty());
    }

    @SuppressWarnings("ParameterHidesMemberVariable")
    @Contract(pure = true)
    public @NotNull ManifestMetadata.GeneratedWith addVersion(
        @NotNull Semver @NotNull ... versions) {
      return new GeneratedWith(
          name(), Stream.concat(this.versions.stream(), Arrays.stream(versions)).toList());
    }
  }

  // the list and its elements must not be null
  // manifest is always using pretty printing
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  private static class GeneratedWithTypeAdapter extends TypeAdapter<List<GeneratedWith>> {

    TypeAdapter<Semver> semverTypeAdapter = SemverTypeAdapter.newInstance(true, semver -> false);

    @Override
    public final void write(
        @NotNull JsonWriter jsonWriter, @NotNull List<@NotNull GeneratedWith> value)
        throws IOException {
      jsonWriter.beginObject();
      for (var generatedWith : value) {
        jsonWriter.name(generatedWith.name()).beginArray();
        jsonWriter.setIndent("");
        for (var semver : generatedWith.versions()) {
          semverTypeAdapter.write(jsonWriter, semver);
        }
        jsonWriter.endArray();
        jsonWriter.setIndent(GsonConstant.PRETTY_PRINTING_INDENT);
      }
      jsonWriter.endObject();
    }

    @Override
    public final @NotNull @UnmodifiableView List<@NotNull GeneratedWith> read(
        @NotNull JsonReader jsonReader) throws IOException {
      jsonReader.beginObject();
      List<ManifestMetadata.GeneratedWith> generatedWithList = new ArrayList<>();
      while (jsonReader.hasNext()) {
        var name = jsonReader.nextName();
        jsonReader.beginArray();
        List<Semver> versions = new ArrayList<>();
        while (jsonReader.hasNext()) {
          versions.add(semverTypeAdapter.read(jsonReader));
        }
        jsonReader.endArray();
        generatedWithList.add(
            new ManifestMetadata.GeneratedWith(name, Collections.unmodifiableList(versions)));
      }
      jsonReader.endObject();
      return Collections.unmodifiableList(generatedWithList);
    }
  }
}
