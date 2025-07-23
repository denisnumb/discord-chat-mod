package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.discord.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMemberData;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsTransceiver;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow @Final EditBox input;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private static int getLastWordIndex(String p_93913_) { return 0; }
    @Shadow public abstract void showSuggestions(boolean p_93931_);


    @Inject(
            method = "updateCommandInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/CommandSuggestions;getLastWordIndex(Ljava/lang/String;)I",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void suggestMentions(CallbackInfo ci) throws CommandSyntaxException {
        String currentInput = input.getValue().substring(0, input.getCursorPosition());
        int lastWordIndex = getLastWordIndex(currentInput);

        if (currentInput.substring(lastWordIndex).startsWith("@")){
            this.pendingSuggestions = MENTIONS_PROVIDER.getSuggestions(null, new SuggestionsBuilder(currentInput, lastWordIndex));
            showSuggestions(true);
            if (!pendingSuggestions.join().isEmpty())
                ci.cancel();
        }

        if (!PlatformConfig.getConfig().isEmojifulCompatibilityEnabled()
                && currentInput.substring(lastWordIndex).startsWith(":")){
            this.pendingSuggestions = EMOJIS_PROVIDER.getSuggestions(null, new SuggestionsBuilder(currentInput, lastWordIndex));
            showSuggestions(true);
            if (!pendingSuggestions.join().isEmpty())
                ci.cancel();
        }
    }

    @Unique
    private static final SuggestionProvider<String> EMOJIS_PROVIDER = (context, builder) -> {
        DiscordEmojisTransceiver.requestDiscordEmojis();

        String partial = builder.getRemaining().toLowerCase().substring(1);

        Stream<String> customEmojis = CustomEmojiProvider.CLIENT_EMOJI_CACHE.keySet().stream()
                .map(emojiName -> ":" + emojiName + ":")
                .filter(emojiName -> emojiName.toLowerCase().contains(partial));

        Stream<String> unicodeEmojis = EmojiManager.getAll().stream()
                .filter(emoji -> emoji.getAliases().stream().anyMatch(alias -> alias.contains(partial)))
                .map(Emoji::getUnicode);

        Stream.concat(customEmojis, unicodeEmojis).forEach(builder::suggest);

        return builder.buildFuture();
    };

    @Unique
    private static boolean discord_minecraft_chat$matchesPartial(DiscordMemberData member, String partial) {
        return member.guildNickname.toLowerCase().contains(partial)
                || member.discordNickName.toLowerCase().contains(partial)
                || member.discordName.toLowerCase().contains(partial);
    }

    @Unique
    private static final SuggestionProvider<String> MENTIONS_PROVIDER = (context, builder) -> {
        DiscordMentionsTransceiver.requestDiscordMemberData();

        String partial = builder.getRemaining().toLowerCase().substring(1);
        StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());

        List<Suggestion> suggestions = ChannelMembersProvider.clientMemberData.stream()
                .filter(data -> discord_minecraft_chat$matchesPartial(data, partial))
                .map(data -> new Suggestion(range, data.getPrettyMention()))
                .toList();

        return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
    };
}
