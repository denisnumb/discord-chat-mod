package com.denisnumb.discord_chat_mod.forge.network;

import net.minecraft.network.FriendlyByteBuf;

public class CustomPayloadPacketsForge {
    public static class DiscordEmojisPartPacketForge extends BasePartPacketForge {
        public DiscordEmojisPartPacketForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public DiscordEmojisPartPacketForge(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class ScreenshotPartPacketServerForge extends BasePartPacketForge {
        public ScreenshotPartPacketServerForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public ScreenshotPartPacketServerForge(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class ScreenshotPartPacketClientForge extends BasePartPacketForge {
        public ScreenshotPartPacketClientForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public ScreenshotPartPacketClientForge(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class DiscordMentionsPartPacketForge extends BasePartPacketForge {
        public DiscordMentionsPartPacketForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public DiscordMentionsPartPacketForge(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }
    }

    public static class RequestDiscordMentionsPacketForge extends BaseRequestPacketForge {
        public RequestDiscordMentionsPacketForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public RequestDiscordMentionsPacketForge() {
            super();
        }
    }

    public static class RequestDiscordEmojisPacketForge extends BaseRequestPacketForge {
        public RequestDiscordEmojisPacketForge(FriendlyByteBuf buffer) {
            super(buffer);
        }

        public RequestDiscordEmojisPacketForge() {
            super();
        }
    }

    public static class CustomPacketPayloadForge {}

    private static class BasePartPacketForge extends CustomPacketPayloadForge {
        public final long sendTime;
        public final int partIndex;
        public final int totalParts;
        public final byte[] data;

        public BasePartPacketForge(long sendTime, int partIndex, int totalParts, byte[] data) {
            this.sendTime = sendTime;
            this.partIndex = partIndex;
            this.totalParts = totalParts;
            this.data = data;
        }

        public BasePartPacketForge(FriendlyByteBuf buffer) {
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

    private static class BaseRequestPacketForge extends CustomPacketPayloadForge {
        public BaseRequestPacketForge() {}
        public BaseRequestPacketForge(FriendlyByteBuf buffer) {}
        public void encode(FriendlyByteBuf buffer) {}
    }
}
