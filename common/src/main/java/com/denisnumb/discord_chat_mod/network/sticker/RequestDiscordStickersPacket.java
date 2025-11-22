package com.denisnumb.discord_chat_mod.network.sticker;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.MOD_ID;

public class RequestDiscordStickersPacket implements CustomPacketPayload {
    public static final Type<RequestDiscordStickersPacket> TYPE
            = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "/network/request_discord_stickers_packet"));


    public static final StreamCodec<ByteBuf, RequestDiscordStickersPacket> STREAM_CODEC = new StreamCodec<>() {
        public void encode(@NotNull ByteBuf buffer, @NotNull RequestDiscordStickersPacket requestDiscordMentionsPacket) {}
        public @NotNull RequestDiscordStickersPacket decode(@NotNull ByteBuf buffer) {
            return new RequestDiscordStickersPacket();
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
