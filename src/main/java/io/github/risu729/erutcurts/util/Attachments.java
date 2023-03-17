/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util;

import static com.google.common.base.Preconditions.checkState;

import io.github.risu729.erutcurts.util.file.FileUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Attachments {

  @CheckReturnValue
  public @NotNull List<@NotNull Path> download(
      @NotNull Collection<? extends Message.@NotNull Attachment> attachments,
      @NotNull Path dir,
      boolean assignUniqueName) {
    return attachments.stream()
        .map(attachment -> download(attachment, dir, assignUniqueName))
        .toList();
  }

  @CheckReturnValue
  public @NotNull Path download(
      Message.@NotNull Attachment attachment, @NotNull Path dir, boolean assignUniqueName) {
    var path = dir.resolve(attachment.getFileName());
    if (!assignUniqueName) {
      checkState(!Files.exists(path), "File already exists: %s", path);
    }
    return attachment
        .getProxy()
        .downloadToPath(assignUniqueName ? FileUtil.generateUniquePathInDir(path) : path)
        .join();
  }

  @Contract(pure = true)
  public @NotNull List<Message.@NotNull Attachment> getAttachmentsWithExtension(
      @NotNull Message message, @NotNull String extension) {
    return message.getAttachments().stream()
        .filter(attachment -> FileUtil.isExtension(attachment.getFileName(), extension))
        .toList();
  }
}
