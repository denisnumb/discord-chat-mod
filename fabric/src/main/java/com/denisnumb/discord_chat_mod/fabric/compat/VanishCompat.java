package com.denisnumb.discord_chat_mod.fabric.compat;

import com.denisnumb.discord_chat_mod.compat.IVanishCompat;
import com.denisnumb.discord_chat_mod.compat.VanishCompatProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.buildPlayerParameters;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.getDiscordMessageComponents;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.handleDiscord;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.sendMessageFromServer;

public class VanishCompat implements IVanishCompat {

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("melius-vanish")) {
            VanishCompatProvider.set(new VanishCompat());
            registerVanishEventListener();
        }
    }

    @Override
    public boolean isVanished(Player player) {
        return VanishAPI.isVanished(player);
    }

    @Override
    public String[] filterVanishedPlayers(MinecraftServer server, String[] playerNames) {
        if (server == null || server.getPlayerList() == null) return playerNames;
        return Arrays.stream(playerNames)
                .filter(name -> {
                    ServerPlayer player = server.getPlayerList().getPlayerByName(name);
                    return player == null || !VanishAPI.isVanished(player);
                })
                .toArray(String[]::new);
    }

    @Override
    public int getVisiblePlayerCount(MinecraftServer server) {
        if (server == null || server.getPlayerList() == null) return 0;
        return (int) server.getPlayerList().getPlayers().stream()
                .filter(player -> !VanishAPI.isVanished(player))
                .count();
    }

    private static void registerVanishEventListener() {
        VanishEvents.VANISH_EVENT.register((player, vanish) -> {
            handleDiscord(() -> {
                MessageType messageType = vanish ? MessageType.LEFT : MessageType.JOIN;
                Map<String, String> parameters = buildPlayerParameters(player);
                getDiscordMessageComponents(messageType, parameters)
                        .ifPresent(components -> sendMessageFromServer(ChannelCategory.PLAYER_JOIN_LEAVE, getAllContexts(), components));
                updateServerStatusWithDelay();
            });
        });
    }
}
