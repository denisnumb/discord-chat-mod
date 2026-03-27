package com.denisnumb.discord_chat_mod.network.emoji;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.MOD_ID;

public record DiscordEmojisPartPacket(
        long sendTime,
        int partIndex,
        int totalParts,
        byte[] data
) implements CustomPacketPayload {
    public static final Type<DiscordEmojisPartPacket> TYPE
            = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "/network/discord_emojis_part_packet"));


    public static final StreamCodec<ByteBuf, DiscordEmojisPartPacket> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull DiscordEmojisPartPacket decode(@NotNull ByteBuf buffer) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            return new DiscordEmojisPartPacket(
                    buf.readLong(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readByteArray()
            );
        }

        public void encode(@NotNull ByteBuf buffer, DiscordEmojisPartPacket packet) {
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
