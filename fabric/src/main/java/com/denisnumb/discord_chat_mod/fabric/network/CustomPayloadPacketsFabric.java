package com.denisnumb.discord_chat_mod.fabric.network;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;

public class CustomPayloadPacketsFabric {
    public static class RequestDiscordMentionsPacketFabric extends RequestDiscordMentionsPacket implements FabricPacket {
        public static final PacketType<RequestDiscordMentionsPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/request_discord_mentions_packet"), RequestDiscordMentionsPacketFabric::new);

        public RequestDiscordMentionsPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public RequestDiscordMentionsPacketFabric(){
            super();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class RequestDiscordEmojisPacketFabric extends RequestDiscordEmojisPacket implements FabricPacket {
        public static final PacketType<RequestDiscordEmojisPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/request_discord_emojis_packet"), RequestDiscordEmojisPacketFabric::new);

        public RequestDiscordEmojisPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public RequestDiscordEmojisPacketFabric(){
            super();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class RequestDiscordStickersPacketFabric extends RequestDiscordStickersPacket implements FabricPacket {
        public static final PacketType<RequestDiscordEmojisPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/request_discord_stickers_packet"), RequestDiscordEmojisPacketFabric::new);

        public RequestDiscordStickersPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public RequestDiscordStickersPacketFabric(){
            super();
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class ImagePartPacketServerFabric extends ImagePartPacketServer implements FabricPacket {
        public static final PacketType<ImagePartPacketServerFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/screenshot_part_packet_server"), ImagePartPacketServerFabric::new);

        public ImagePartPacketServerFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public ImagePartPacketServerFabric(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class ImagePartPacketClientFabric extends ImagePartPacketClient implements FabricPacket {
        public static final PacketType<ImagePartPacketClientFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/screenshot_part_packet_client"), ImagePartPacketClientFabric::new);

        public ImagePartPacketClientFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public ImagePartPacketClientFabric(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class DiscordMentionsPartPacketFabric extends DiscordMentionsPartPacket implements FabricPacket {
        public static final PacketType<DiscordMentionsPartPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/discord_mentions_packet"), DiscordMentionsPartPacketFabric::new);

        public DiscordMentionsPartPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public DiscordMentionsPartPacketFabric(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class DiscordEmojisPartPacketFabric extends DiscordEmojisPartPacket implements FabricPacket {
        public static final PacketType<DiscordEmojisPartPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/discord_emojis_packet"), DiscordEmojisPartPacketFabric::new);

        public DiscordEmojisPartPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public DiscordEmojisPartPacketFabric(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }

    public static class DiscordStickersPartPacketFabric extends DiscordStickersPartPacket implements FabricPacket {
        public static final PacketType<DiscordEmojisPartPacketFabric> TYPE =
                PacketType.create(new ResourceLocation(DiscordChatMod.MOD_ID, "network/discord_stickers_packet"), DiscordEmojisPartPacketFabric::new);

        public DiscordStickersPartPacketFabric(FriendlyByteBuf buf){
            super(buf);
        }

        public DiscordStickersPartPacketFabric(long sendTime, int partIndex, int totalParts, byte[] data){
            super(sendTime, partIndex, totalParts, data);
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            super.encode(buf);
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }
}
