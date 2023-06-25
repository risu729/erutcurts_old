/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.erutcurts.util.interaction;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Table;
import io.github.risu729.erutcurts.misc.Notifications;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.GenericAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

// automatically send stacktrace on command interaction, for other interactions do it manually
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Slf4j
public abstract class ListenerWithRegistry extends ListenerAdapter {

  private static final List<Class<? extends Executable<?>>> EXECUTABLE_TYPES =
      List.of(
          ExecutableSlashCommandData.class,
          ExecutableUserCommandData.class,
          ExecutableMessageCommandData.class,
          ExecutableButton.class,
          ExecutableModal.class,
          ExecutableStringSelectMenu.class,
          ExecutableEntitySelectMenu.class);

  @NotNull Table<Class<?>, String, Executable<?>> registry;

  // row is command name, column is option name
  @NotNull Table<String, String, ExecutableOptionData> optionRegistry;

  // register commands
  protected ListenerWithRegistry(@NotNull Executable<?> @NotNull ... executables) {

    checkArgument(
        Arrays.stream(executables)
            .allMatch(
                executable ->
                    EXECUTABLE_TYPES.stream().anyMatch(type -> type.isInstance(executable))));

    registry =
        Arrays.stream(executables)
            .collect(
                ImmutableTable.toImmutableTable(
                    executable ->
                        EXECUTABLE_TYPES.stream()
                            .filter(type -> type.isInstance(executable))
                            .collect(MoreCollectors.onlyElement()),
                    Executable::getKey,
                    Function.identity()));

    var builder = ImmutableTable.<String, String, ExecutableOptionData>builder();
    Arrays.stream(executables)
        .filter(SlashCommandData.class::isInstance)
        .map(SlashCommandData.class::cast)
        .forEach(
            command ->
                command.getOptions().stream()
                    .filter(OptionData::isAutoComplete)
                    // all auto complete options must be executable
                    .map(ExecutableOptionData.class::cast)
                    .forEach(option -> builder.put(command.getName(), option.getName(), option)));
    optionRegistry = builder.buildOrThrow();
  }

  public final @NotNull @Unmodifiable List<? extends CommandData> getGlobalCommands() {
    return Stream.of(
            ExecutableSlashCommandData.class,
            ExecutableUserCommandData.class,
            ExecutableMessageCommandData.class)
        .flatMap(type -> registry.row(type).values().stream().map(type::cast))
        .filter(
            command ->
                !(command instanceof ExecutableSlashCommandData slashCommand
                    && slashCommand.isAdminGuildOnly()))
        .toList();
  }

  public final @NotNull @Unmodifiable List<ExecutableSlashCommandData> getAdminCommands() {
    return registry.row(ExecutableSlashCommandData.class).values().stream()
        .map(ExecutableSlashCommandData.class::cast)
        .filter(ExecutableSlashCommandData::isAdminGuildOnly)
        .toList();
  }

  // do not reply stacktrace for auto-complete because it is meaningless
  @Override
  public final void onCommandAutoCompleteInteraction(
      @Nonnull CommandAutoCompleteInteractionEvent event) {
    Optional.ofNullable(optionRegistry.get(event.getName(), event.getFocusedOption().getName()))
        .ifPresent(option -> option.execute(event));
  }

  @Override
  public final void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    onInteraction(ExecutableSlashCommandData.class, event);
  }

  @Override
  public final void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
    onInteraction(ExecutableUserCommandData.class, event);
  }

  @Override
  public final void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
    onInteraction(ExecutableMessageCommandData.class, event);
  }

  @Override
  public final void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
    onInteraction(ExecutableButton.class, event);
  }

  @Override
  public final void onModalInteraction(@Nonnull ModalInteractionEvent event) {
    onInteraction(ExecutableModal.class, event);
  }

  @Override
  public final void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
    onInteraction(ExecutableStringSelectMenu.class, event);
  }

  @Override
  public final void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {
    onInteraction(ExecutableEntitySelectMenu.class, event);
  }

  public void shutdown() {
    // do nothing
  }

  private <T extends Executable<E>, E extends GenericInteractionCreateEvent & IReplyCallback>
      void onInteraction(@Nonnull Class<T> executableType, @NotNull E event) {

    checkArgument(EXECUTABLE_TYPES.contains(executableType));

    String key;
    if (event instanceof GenericCommandInteractionEvent commandInteractionEvent) {
      key = commandInteractionEvent.getName();
    } else if (event instanceof GenericComponentInteractionCreateEvent componentInteractionEvent) {
      key = componentInteractionEvent.getComponentId();
    } else if (event instanceof ModalInteractionEvent modalInteractionEvent) {
      key = modalInteractionEvent.getModalId();
    } else {
      throw new AssertionError();
    }

    try {
      // don't throw exception when command not found because there might be other registries
      Optional.ofNullable(registry.get(executableType, key))
          .map(executableType::cast)
          .ifPresent(
              executable -> {
                Notifications.sendLog(
                    "Executed interaction: %s".formatted(executable.getKey()),
                    event.getGuild(),
                    event.getUser());
                executable.execute(event);
              });
    } catch (RuntimeException | Error exception) {
      Notifications.replyStackTrace(event, exception);
      throw exception;
    }
  }

  // do not allow to override these onGeneric*Event methods
  @Override
  public final void onGenericInteractionCreate(@Nonnull GenericInteractionCreateEvent event) {}

  @Override
  public final void onGenericAutoCompleteInteraction(
      @Nonnull GenericAutoCompleteInteractionEvent event) {}

  @Override
  public final void onGenericComponentInteractionCreate(
      @Nonnull GenericComponentInteractionCreateEvent event) {}

  @Override
  public final void onGenericCommandInteraction(@Nonnull GenericCommandInteractionEvent event) {}

  @Override
  public final void onGenericContextInteraction(@Nonnull GenericContextInteractionEvent<?> event) {}

  @SuppressWarnings({"rawtypes", "RedundantSuppression"})
  @Override
  public final void onGenericSelectMenuInteraction(
      @Nonnull GenericSelectMenuInteractionEvent event) {}
}
