package com.denisnumb.discord_chat_mod.network.screenshot;

import com.denisnumb.discord_chat_mod.markdown.tellraw.TellRawComponent;
import com.denisnumb.discord_chat_mod.markdown.tellraw.ComponentEvent;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import com.mojang.datafixers.util.Either;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.ColorUtils.getHexColor;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;

public class ScreenshotTransceiver {
    private static final ExecutorService networkPool = Executors.newSingleThreadExecutor();
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static long lastScreenshotId = 0;
    private static String lastFileName = null;

    public static void sendScreenshot(File screenshotFile, Player player) {
        long screenshotId = System.currentTimeMillis();
        String fileName = screenshotFile.getName();

        if (fileName.equals(lastFileName) && screenshotId - lastScreenshotId < 20000)
            return;
        if (screenshotId - lastScreenshotId < 2000)
            return;

        try{
            byte[] screenshotBytes = Files.readAllBytes(screenshotFile.toPath());
            lastFileName = fileName;
            lastScreenshotId = screenshotId;

            BigPacketsTransceiver.send(screenshotBytes, (partIndex, totalParts, part) ->
                    PacketDistributor.sendToServer(new ScreenshotPartPacket(screenshotId, partIndex, totalParts, part))
            );
        } catch (Exception e){
            sendErrorMessageToPlayer(player, e.getMessage());
        }
    }

    public static void receivePart(ScreenshotPartPacket packet, ServerPlayer player) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.imageId(),
                packet.partIndex(),
                packet.totalParts(),
                packet.data()
        ).ifPresent(data -> networkPool.execute(() -> sendScreenshotToDiscord(data, player)));
    }

    private static void sendScreenshotToDiscord(byte[] screenshotBytes, Player player) {
        Optional<MessageCreateAction> createActionOptional = prepareDiscordTextMessage("`<" + player.getName().getString() + ">`");

        if (createActionOptional.isEmpty()) {
            sendErrorMessageToPlayer(player, null);
            return;
        }

        Either<Message, Optional<ErrorResponseException>> result = sendMessageComplete(
                createActionOptional.get().addFiles(FileUpload.fromData(screenshotBytes, System.currentTimeMillis() + ".png"))
        );

        result.ifLeft(message -> handleSuccessful(message, player));
        result.ifRight(errorOpt -> handleError(errorOpt, player));
    }

    private static void handleSuccessful(Message message, Player player) {
        executeServerCommand(prepareTellRawCommand(new ArrayList<>() {{
            add(new TellRawComponent("<" + player.getName().getString() + "> "));
            add(new TellRawComponent(getTranslate(SCREENSHOT, "Screenshot"))
                    .setColor(getHexColor(CHAT_LINK_COLOR))
                    .addClickEvent(new ComponentEvent("open_url", message.getAttachments().getFirst().getUrl()))
                    .addHoverEvent(new ComponentEvent("show_text", message.getAttachments().getFirst().getUrl()))
            );
        }}));
    }

    private static void handleError(Optional<ErrorResponseException> errorOpt, Player player) {
        errorOpt.ifPresentOrElse(
                error -> sendErrorMessageToPlayer(player, error.getMeaning()),
                () -> sendErrorMessageToPlayer(player, null)
        );
    }

    private static void sendErrorMessageToPlayer(Player player, @Nullable String errorMessage){
        String message = errorMessage == null
                ? getTranslate(SCREENSHOT_SENDING_ERROR, "There was an error sending the screenshot")
                : String.format(getTranslate(SCREENSHOT_SENDING_ERROR_WITH_REASON, "There was an error sending the screenshot: %s"), errorMessage);

        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }
}
