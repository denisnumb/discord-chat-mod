package com.denisnumb.discord_chat_mod.network.screenshot;

import com.denisnumb.discord_chat_mod.markdown.tellraw.TellRawComponent;
import com.denisnumb.discord_chat_mod.markdown.tellraw.ComponentEvent;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.ColorUtils.getHexColor;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.discordChannel;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.executeServerCommand;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SCREENSHOT;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.prepareTellRawCommand;

public class ScreenshotTransceiver {
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static long lastScreenshotId = 0;

    public static void sendScreenshot(byte[] screenshotBytes) {
        long screenshotId = System.currentTimeMillis();
        if (screenshotId - lastScreenshotId < 2000)
            return;
        lastScreenshotId = screenshotId;

        BigPacketsTransceiver.send(screenshotBytes, screenshotId, (sendTime, partIndex, totalParts, part) ->
                PacketDistributor.sendToServer(new ScreenshotPartPacket(screenshotId, partIndex, totalParts, part))
        );
    }

    public static void receivePart(ScreenshotPartPacket packet, ServerPlayer player) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.imageId(),
                packet.partIndex(),
                packet.totalParts(),
                packet.data()
        ).ifPresent(data -> {
            Message message = discordChannel.sendMessage("`<" + player.getName().getString() + ">`")
                    .addFiles(FileUpload.fromData(data, System.currentTimeMillis() + ".png"))
                    .complete();

            executeServerCommand(prepareTellRawCommand(new ArrayList<>(){{
                add(new TellRawComponent("<" + player.getName().getString() + "> "));
                add(new TellRawComponent(getTranslate(SCREENSHOT, "Screenshot"))
                        .setColor(getHexColor(CHAT_LINK_COLOR))
                        .addClickEvent(new ComponentEvent("open_url", message.getAttachments().getFirst().getUrl()))
                        .addHoverEvent(new ComponentEvent("show_text", message.getAttachments().getFirst().getUrl()))
                );
            }}));
        });
    }
}
