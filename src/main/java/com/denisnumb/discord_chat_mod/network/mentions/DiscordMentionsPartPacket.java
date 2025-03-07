package com.denisnumb.discord_chat_mod.network.mentions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DiscordMentionsPartPacket {
    public final long sendTime;
    public final int partIndex;
    public final int totalParts;
    public final byte[] data;

    public DiscordMentionsPartPacket(long sendTime, int partIndex, int totalParts, byte[] data){
        this.sendTime = sendTime;
        this.partIndex = partIndex;
        this.totalParts = totalParts;
        this.data = data;
    }

    public DiscordMentionsPartPacket(FriendlyByteBuf buffer) {
        this.sendTime = buffer.readLong();
        this.partIndex = buffer.readInt();
        this.totalParts = buffer.readInt();
        this.data = buffer.readByteArray();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(sendTime);
        buffer.writeInt(partIndex);
        buffer.writeInt(totalParts);
        buffer.writeByteArray(data);
    }

    public static void handle(DiscordMentionsPartPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        DiscordMentionsTransceiver.receiveDiscordMentionsPart(packet);
        context.setPacketHandled(true);
    }
}
