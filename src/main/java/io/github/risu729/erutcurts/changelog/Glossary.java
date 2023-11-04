/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 *//*


   package io.github.risu729.erutcurts.changelog;

   import com.deepl.api.DeepLException;
   import com.deepl.api.GlossaryLanguagePair;
   import com.google.gson.reflect.TypeToken;
   import io.github.risu729.erutcurts.DiscordDB;
   import io.github.risu729.erutcurts.misc.Notifications;
   import io.github.risu729.erutcurts.util.interaction.ExecutableSlashCommandData;
   import io.github.risu729.erutcurts.util.localization.CommandLocalization;
   import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
   import net.dv8tion.jda.api.interactions.DiscordLocale;
   import net.dv8tion.jda.api.interactions.InteractionHook;
   import net.dv8tion.jda.api.interactions.commands.OptionType;
   import org.jetbrains.annotations.NotNull;

   import java.util.ArrayList;
   import java.util.List;

   import static com.google.common.base.Preconditions.checkArgument;
   import static com.google.common.base.Preconditions.checkState;

   @SuppressWarnings("ProhibitedExceptionThrown")
   public final class Glossary extends ExecutableSlashCommandData {

     private static final String COMMAND_NAME = "glossary";
     private static final String COMMAND_WORD = "word";
     private static final String COMMAND_TRANSLATION = "translation";

     private static final String TRUSTED_USERS_DATABASE_NAME = "TrustedUsers";
     private static final String GLOSSARY_NAME = "minecraft";

     public Glossary() {
       super(CommandLocalization.createSlashCommand(COMMAND_NAME)
           .addOptions(CommandLocalization.createOption(OptionType.STRING, COMMAND_WORD)
                   .setRequired(true),
               CommandLocalization.createOption(OptionType.STRING, COMMAND_TRANSLATION)
                   .setRequired(true)));
     }

     @Override
     public void execute(@NotNull InteractionHook hook,
         @NotNull GenericCommandInteractionEvent event) {
       List<String> trustedUserIds = DiscordDB.get(TRUSTED_USERS_DATABASE_NAME,
           new TypeToken<>() {},
           ArrayList::new);
       Notifications.sendNotification();
     }

     static void add(@NotNull String word, @NotNull DiscordLocale locale, @NotNull String translation) {
       var targetLang = DeepLTranslator.DISCORD_LOCALE_TARGET_LANGUAGE_MAP.get(locale);
       checkArgument(targetLang != null, "%s is not supported by DeepL".formatted(locale));
       DeepLTranslator.TRANSLATOR.getGlossaryLanguages().contains(new GlossaryLanguagePair())
       try {
         DeepLTranslator.TRANSLATOR.listGlossaries().stream()
             .filter(glossary -> glossary.getName().equals(GLOSSARY_NAME))
             .filter(glossary -> glossary.getSourceLang().equals(DeepLTranslator.DISCORD_LOCALE_TARGET_LANGUAGE_MAP.get(locale))
       } catch (DeepLException | InterruptedException e) {
         throw new RuntimeException(e);
       }
     }
   }
   */
