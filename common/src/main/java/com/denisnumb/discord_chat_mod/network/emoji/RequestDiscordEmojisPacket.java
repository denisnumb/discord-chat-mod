package com.denisnumb.discord_chat_mod.network.emoji;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.MOD_ID;


public class RequestDiscordEmojisPacket implements CustomPacketPayload {
    public static final Type<RequestDiscordEmojisPacket> TYPE
            = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "/network/request_discord_emojis_packet"));


    public static final StreamCodec<ByteBuf, RequestDiscordEmojisPacket> STREAM_CODEC = new StreamCodec<>() {
        public void encode(@NotNull ByteBuf buffer, @NotNull RequestDiscordEmojisPacket requestDiscordMentionsPacket) {}
        public @NotNull RequestDiscordEmojisPacket decode(@NotNull ByteBuf buffer) {
            return new RequestDiscordEmojisPacket();
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
