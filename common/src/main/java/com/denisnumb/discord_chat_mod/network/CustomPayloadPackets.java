package com.denisnumb.discord_chat_mod.network;

import net.minecraft.network.FriendlyByteBuf;

public class CustomPayloadPackets {
    public static class DiscordEmojisPartPacket extends BasePartPacket {
        public DiscordEmojisPartPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public DiscordEmojisPartPacket(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class DiscordStickersPartPacket extends BasePartPacket {
        public DiscordStickersPartPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public DiscordStickersPartPacket(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class ScreenshotPartPacketServer extends BasePartPacket {
        public ScreenshotPartPacketServer(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public ScreenshotPartPacketServer(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class ScreenshotPartPacketClient extends BasePartPacket {
        public ScreenshotPartPacketClient(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public ScreenshotPartPacketClient(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class DiscordMentionsPartPacket extends BasePartPacket {
        public DiscordMentionsPartPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public DiscordMentionsPartPacket(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class RequestDiscordMentionsPacket extends BaseRequestPacket {
        public RequestDiscordMentionsPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public RequestDiscordMentionsPacket() {
            super();
        }
    }

    public static class RequestDiscordEmojisPacket extends BaseRequestPacket {
        public RequestDiscordEmojisPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public RequestDiscordEmojisPacket() {
            super();
        }
    }

    public static class RequestDiscordStickersPacket extends BaseRequestPacket {
        public RequestDiscordStickersPacket(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public RequestDiscordStickersPacket() {
            super();
        }
    }

    public static class CustomPacketPayload { }

    public static class BasePartPacket extends CustomPacketPayload {
        public final long sendTime;
        public final int partIndex;
        public final int totalParts;
        public final byte[] data;

        public BasePartPacket(long sendTime, int partIndex, int totalParts, byte[] data) {
            this.sendTime = sendTime;
            this.partIndex = partIndex;
            this.totalParts = totalParts;
            this.data = data;
        }

        public BasePartPacket(FriendlyByteBuf buffer) {
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
    }

    public static abstract class BaseRequestPacket extends CustomPacketPayload {
        public BaseRequestPacket() {}
        public BaseRequestPacket(FriendlyByteBuf buffer) {}
        public void encode(FriendlyByteBuf buffer) {}
    }
}
