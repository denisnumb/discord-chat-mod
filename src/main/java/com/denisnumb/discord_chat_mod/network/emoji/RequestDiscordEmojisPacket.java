package com.denisnumb.discord_chat_mod.network.emoji;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestDiscordEmojisPacket {
    public RequestDiscordEmojisPacket(){}
    public RequestDiscordEmojisPacket(FriendlyByteBuf buffer) {}
    public void encode(FriendlyByteBuf buffer) {}

    public static void handle(RequestDiscordEmojisPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null)
                DiscordEmojisTransceiver.sendDiscordEmojisDataToPlayer(player);
        });
        context.setPacketHandled(true);
    }
}
