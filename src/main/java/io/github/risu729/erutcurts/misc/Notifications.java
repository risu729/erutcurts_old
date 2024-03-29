/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.misc;

import com.google.common.base.Throwables;
import io.github.risu729.erutcurts.Erutcurts;
import io.github.risu729.erutcurts.util.Attachments;
import io.github.risu729.erutcurts.util.EmbedUtil;
import io.github.risu729.erutcurts.util.Envs;
import io.github.risu729.erutcurts.util.file.CloseablePath;
import io.github.risu729.erutcurts.util.file.FileUtil;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Notifications {

  @SuppressWarnings("WeakerAccess")
  public void sendNotification(@NotNull String message) {
    sendToNotificationChannel(
        EmbedUtil.createDefaultBuilder("Notification").setDescription(message).build());
  }

  public void sendDataRequest(@NotNull DataRequest dataRequest) {
    sendToNotificationChannel(dataRequest.createEmbed());
  }

  public void sendLog(@NotNull String message, @Nullable Guild guild, @NotNull User user) {
    sendLog(message, guild, user, List.of());
  }

  public void sendLog(
      @NotNull String message,
      @Nullable Guild guild,
      @NotNull User user,
      @NotNull Collection<? extends Message.@NotNull Attachment> attachments) {
    sendToNotificationChannel(
        EmbedUtil.createDefaultBuilder("Log")
            .setColor(Color.GREEN)
            .setDescription(message)
            .addField("Guild", guild == null ? "DM" : guild.getName(), false)
            .addField("User", user.getName(), false)
            .build(),
        attachments);
  }

  public void replyStackTrace(@NotNull IReplyCallback callback, @NotNull Throwable throwable) {
    var embed = createStackTraceEmbed(throwable);
    if (callback.isAcknowledged()) {
      callback.getHook().sendMessageEmbeds(embed).queue();
    } else {
      callback.replyEmbeds(embed).setEphemeral(true).queue();
    }
    sendToNotificationChannel(embed);
  }

  public void replyStackTrace(@NotNull Message message, @NotNull Throwable throwable) {
    var embed = createStackTraceEmbed(throwable);
    message.replyEmbeds(embed).queue();
    sendToNotificationChannel(embed);
  }

  @Contract("_ -> new")
  private @NotNull MessageEmbed createStackTraceEmbed(@NotNull Throwable throwable) {
    var stackTrace = Throwables.getStackTraceAsString(throwable);
    return EmbedUtil.createDefaultBuilder("Error")
        .setColor(Color.RED)
        .setDescription(
            stackTrace.length() <= MessageEmbed.DESCRIPTION_MAX_LENGTH
                ? stackTrace
                : stackTrace.substring(0, MessageEmbed.DESCRIPTION_MAX_LENGTH))
        .build();
  }

  private void sendToNotificationChannel(@NotNull MessageEmbed embed) {
    sendToNotificationChannel(embed, List.of());
  }

  private void sendToNotificationChannel(
      @NotNull MessageEmbed embed,
      @NotNull Collection<? extends Message.@NotNull Attachment> attachments) {
    var action =
        Erutcurts.getMessageChannelById(Envs.getEnv("NOTIFICATION_CHANNEL_ID"))
            .sendMessageEmbeds(embed);
    try (var closeableTempDir = CloseablePath.of(FileUtil.createTempDir())) {
      action
          .addFiles(
              Attachments.download(attachments, closeableTempDir.path(), true).stream()
                  .map(FileUpload::fromData)
                  .toList())
          .queue();
    } catch (RuntimeException e) {
      action.queue();
    }
  }
}
