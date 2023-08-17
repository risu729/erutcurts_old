/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.structure;

import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.misc.Notifications;
import io.github.risu729.erutcurts.misc.Settings;
import io.github.risu729.erutcurts.util.Attachments;
import io.github.risu729.erutcurts.util.interaction.ListenerWithRegistry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public final class StructureListener extends ListenerWithRegistry {

  public StructureListener() {
    super(PackageMode.getInstance(), Convert.getInstance());
  }

  // pack auto-generation
  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {

    var message = event.getMessage();

    try {

      // ignore self messages
      if (Erutcurts.isSelfMessage(message)) {
        return;
      }

      // ignore messages without mcstructure attachments
      var structureAttachments =
          Attachments.getAttachmentsWithExtension(message, MCExtension.MCSTRUCTURE.toString());
      if (structureAttachments.isEmpty()) {
        return;
      }

      // ignore if pack auto-generation is disabled
      if (event.isFromGuild()
          && !Settings.getInstance().isPackAutoGenerationEnabled(event.getGuild().getId())) {
        return;
      }

      // ignore messages in package mode channels
      // check this at last because this method might take time
      if (PackageMode.getInstance().isPackageModeEnabled(event.getChannel().getId())) {
        return;
      }

      // lasts in 10 secs, but this doesn't take that much time
      event.getChannel().sendTyping().queue();

      Notifications.sendLog(
          "Auto-generated pack.",
          event.isFromGuild() ? event.getGuild() : null,
          event.getAuthor(),
          structureAttachments);

      message
          .replyFiles(TargetType.BEHAVIOR.convert(structureAttachments))
          .mentionRepliedUser(false)
          .queue();

    } catch (RuntimeException | Error exception) {
      Notifications.replyStackTrace(message, exception);
      throw exception;
    }
  }
}
