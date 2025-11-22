package com.denisnumb.discord_chat_mod.mixin;


import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V",
                    remap = false
            ),
            method = "<init>"
    )
    private void registerCommands(Commands.CommandSelection environment, CommandBuildContext registryAccess, CallbackInfo ci) {
        MinecraftEvents.handleRegisterCommands(dispatcher, registryAccess);
    }
}
