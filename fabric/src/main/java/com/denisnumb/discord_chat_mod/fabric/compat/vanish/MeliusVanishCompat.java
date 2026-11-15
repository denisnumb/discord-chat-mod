package com.denisnumb.discord_chat_mod.fabric.compat.vanish;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.compat.vanish.IVanishCompat;
import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import me.drex.vanish.config.ConfigManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

final class MeliusVanishCompat implements IVanishCompat {
    static final String MOD_ID = "melius-vanish";

    private static final String STYLED_CHAT_MOD_ID = "styledchat";
    private static final Identifier FAKE_MESSAGE_PHASE = Identifier.fromNamespaceAndPath(DiscordChatMod.MOD_ID, "fake_join_leave");

    static void register() {
        VanishEvents.VANISH_EVENT.register(MinecraftEvents::handleVanishStatusChange);
        registerFakeMessageStyling();
        VanishCompatProvider.set(new MeliusVanishCompat());
    }

    private static void registerFakeMessageStyling() {
        if (FabricLoader.getInstance().isModLoaded(STYLED_CHAT_MOD_ID))
            return;

        VanishEvents.VANISH_MESSAGE_EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, FAKE_MESSAGE_PHASE);
        VanishEvents.VANISH_MESSAGE_EVENT.register(FAKE_MESSAGE_PHASE, player -> fakeJoinLeaveMessage(player, false));

        VanishEvents.UN_VANISH_MESSAGE_EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, FAKE_MESSAGE_PHASE);
        VanishEvents.UN_VANISH_MESSAGE_EVENT.register(FAKE_MESSAGE_PHASE, player -> fakeJoinLeaveMessage(player, true));
    }

    private static Component fakeJoinLeaveMessage(ServerPlayer player, boolean isJoin) {
        return MinecraftEvents.handleVanishFakeJoinLeave(player, isJoin).orElseGet(() ->
                Component.translatable(isJoin ? "multiplayer.player.joined" : "multiplayer.player.left", player.getDisplayName())
                        .withStyle(ChatFormatting.YELLOW)
        );
    }

    @Override
    public String id() {
        return MOD_ID;
    }

    @Override
    public boolean isVanished(ServerPlayer player) {
        return VanishAPI.isVanished(player);
    }

    @Override
    public boolean canSeePlayer(ServerPlayer actor, ServerPlayer observer) {
        return VanishAPI.canSeePlayer(actor, observer);
    }

    @Override
    public boolean announcesFakeJoinLeave() {
        try {
            return ConfigManager.vanish().sendJoinDisconnectMessage;
        } catch (LinkageError | RuntimeException e) {
            return true;
        }
    }

    @Override
    public Optional<Component> getPrivateMessageDenial(ServerPlayer sender) {
        if (!isVanished(sender) || !isMsgDisabled())
            return Optional.empty();

        return Optional.of(Component.translatable("text.vanish.chat.disabled").withStyle(ChatFormatting.RED));
    }

    private static boolean isMsgDisabled() {
        try {
            return ConfigManager.vanish().disableMsg;
        } catch (LinkageError | RuntimeException e) {
            return true;
        }
    }
}
