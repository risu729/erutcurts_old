/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure.behavior.manifest;

import com.google.gson.annotations.SerializedName;

public enum ManifestCapability {
  CHEMISTRY,
  RAYTRACED,
  SCRIPT_EVAL,
  @SerializedName("editorExtension")
  EDITOR_EXTENSION
}
