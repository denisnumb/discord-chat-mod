package com.denisnumb.discord_chat_mod.network.sticker;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.MOD_ID;

public record DiscordStickersPartPacket(
        long sendTime,
        int partIndex,
        int totalParts,
        byte[] data
) implements CustomPacketPayload {
    public static final Type<com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket> TYPE
            = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "/network/discord_stickers_part_packet"));


    public static final StreamCodec<ByteBuf, com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket decode(@NotNull ByteBuf buffer) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            return new com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket(
                    buf.readLong(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readByteArray()
            );
        }

        public void encode(@NotNull ByteBuf buffer, com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket packet) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            buf.writeLong(packet.sendTime);
            buf.writeInt(packet.partIndex);
            buf.writeInt(packet.totalParts);
            buf.writeByteArray(packet.data);
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

