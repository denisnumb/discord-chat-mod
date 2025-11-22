package com.denisnumb.discord_chat_mod.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionsListAccessorMixin {
    @Accessor("rect")
    Rect2i getRect();

    @Accessor("suggestionList")
    List<Suggestion> getSuggestionsList();

    @Accessor("current")
    int getCurrent();
}
