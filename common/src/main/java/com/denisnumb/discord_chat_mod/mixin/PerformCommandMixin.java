package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class PerformCommandMixin {
    @Inject(
            method = "performCommand",
            at = @At("HEAD")
    )
    private void discordChatMod$logCommandExecution(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        MinecraftEvents.handleCommandExecution(parseResults.getContext().getSource(), command);
    }
}
