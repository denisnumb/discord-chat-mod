package com.denisnumb.discord_chat_mod.network.mentions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class RequestDiscordMentionsPacket {
    public RequestDiscordMentionsPacket(){}
    public RequestDiscordMentionsPacket(FriendlyByteBuf buffer) {}
    public void encode(FriendlyByteBuf buffer) {}

    public static void handle(RequestDiscordMentionsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null)
                DiscordMentionsTransceiver.sendDiscordMemberDataToPlayer(player);
        });
        context.setPacketHandled(true);
    }
}
