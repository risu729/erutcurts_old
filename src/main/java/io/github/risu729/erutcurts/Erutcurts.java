/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.risu729.erutcurts.misc.MiscListener;
import io.github.risu729.erutcurts.structure.StructureListener;
import io.github.risu729.erutcurts.util.file.FileUtil;
import io.github.risu729.erutcurts.util.interaction.ListenerWithRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Erutcurts {

  public final OffsetDateTime START_TIME = OffsetDateTime.now(ZoneOffset.UTC);

  public final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir")).resolve(BotInfo.NAME);

  public final Path RESOURCES_DIR = Path.of("src", "main", "resources");

  @Getter private final JDA JDA;

  static {
    // start bot
    JDA =
        JDABuilder.createLight(
                Envs.getEnv("DISCORD_TOKEN"), // no validation needed because JDA will do it
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT)
            .setActivity(Activity.playing("Minecraft Bedrock Edition"))
            .addEventListeners(new StructureListener(), new MiscListener())
            .setEnableShutdownHook(false) // disable to customize shutdown order
            .build();
  }

  public void main(String[] args) throws InterruptedException {

    // do not call awaitReady in static initializer because it throws an exception
    JDA.awaitReady();

    var adminCommands =
        getListeners().stream()
            .map(ListenerWithRegistry::getAdminCommands)
            .flatMap(List::stream)
            .toList();

    checkNotNull(JDA.getGuildById(Envs.getEnv("ADMIN_GUILD_ID")))
        .updateCommands()
        .addCommands(adminCommands)
        .queue();

    var globalCommands =
        getListeners().stream()
            .map(ListenerWithRegistry::getGlobalCommands)
            .flatMap(List::stream)
            .toList();

    JDA.updateCommands().addCommands(globalCommands).queue();

    FileUtil.deleteQuietly(TEMP_DIR);
    try {
      Files.createDirectory(TEMP_DIR);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(Erutcurts::shutdown));
  }

  @Contract(pure = true)
  public @NotNull MessageChannel getMessageChannelById(@NotNull String id) {
    return checkNotNull(getJDA().getChannelById(MessageChannel.class, id));
  }

  @Contract(pure = true)
  public boolean isSelfMessage(@NotNull Message message) {
    return message.getAuthor().getId().equals(getJDA().getSelfUser().getId());
  }

  private void shutdown() {
    getListeners().forEach(ListenerWithRegistry::shutdown);
    DiscordDB.shutdown();
    try {
      Thread.sleep(Duration.ofSeconds(3)); // wait for completion
    } catch (InterruptedException ignored) {
    }
    JDA.shutdown();
    FileUtil.deleteQuietly(TEMP_DIR);
  }

  @Contract(pure = true)
  private @NotNull List<@NotNull ListenerWithRegistry> getListeners() {
    return JDA.getRegisteredListeners().stream().map(ListenerWithRegistry.class::cast).toList();
  }
}
