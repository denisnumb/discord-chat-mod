package com.denisnumb.discord_chat_mod.network.screenshot;

import com.denisnumb.discord_chat_mod.MinecraftUtils;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Either;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.YELLOW;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;

public class ScreenshotTransceiver {
    public record ScreenshotData(String fileName, byte[] data){}
    private static final Gson gson = new Gson();
    private static final ExecutorService networkPool = Executors.newSingleThreadExecutor();
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static long lastSendScreenshotTime = 0;
    private static File lastFile = null;

    public static void sendScreenshot(File screenshotFile, Player player, boolean sendAsSpoiler) {
        long currentTime = System.currentTimeMillis();

        if ((screenshotFile.equals(lastFile) && currentTime - lastSendScreenshotTime < 20000)
                || (currentTime - lastSendScreenshotTime < 2000)
        ){
            MinecraftUtils.showTitleBarMessage(Component.literal(
                    getTranslateClient(SCREENSHOT_COOLDOWN, "Please wait a few seconds before sending the screenshot")
            ).withColor(YELLOW));
            return;
        }

        MinecraftUtils.showTitleBarMessage(Component.literal(getTranslateClient(SENDING_SCREENSHOT, "Sending screenshot...")));
        networkPool.execute(() -> {
            try{
                byte[] screenshotBytes = Files.readAllBytes(screenshotFile.toPath());
                String fileName = sendAsSpoiler
                        ? "SPOILER_" + screenshotFile.getName()
                        : screenshotFile.getName();
                byte[] data = gson.toJson(new ScreenshotData(fileName, screenshotBytes)).getBytes();
                lastFile = screenshotFile;
                lastSendScreenshotTime = currentTime;

                BigPacketsTransceiver.send(data, (partIndex, totalParts, part) ->
                        PacketDistributor.sendToServer(new ScreenshotPartPacket(currentTime, partIndex, totalParts, part))
                );
            } catch (Exception e){
                sendErrorMessageToPlayer(player, e.getMessage());
            }
        });
    }

    public static void receivePart(ScreenshotPartPacket packet, ServerPlayer player) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.imageId(),
                packet.partIndex(),
                packet.totalParts(),
                packet.data()
        ).ifPresent(rawData -> networkPool.execute(() -> sendScreenshotToDiscord(rawData, player)));
    }

    private static void sendScreenshotToDiscord(byte[] rawScreenshotData, Player player) {
        ScreenshotData screenshotData = gson.fromJson(new String(rawScreenshotData), ScreenshotData.class);
        Optional<MessageCreateAction> createActionOptional = prepareDiscordTextMessage("`<" + player.getName().getString() + ">`");

        if (createActionOptional.isEmpty()) {
            sendErrorMessageToPlayer(player, null);
            return;
        }

        Either<Message, Optional<ErrorResponseException>> result = sendMessageComplete(
                createActionOptional.get().addFiles(FileUpload.fromData(screenshotData.data, screenshotData.fileName))
        );

        result.ifLeft(message -> handleSuccessful(message, player));
        result.ifRight(errorOpt -> handleError(errorOpt, player));
    }

    private static void handleSuccessful(Message message, Player player) {
        sendMessageToAllPlayers(
                Component.literal("<" + player.getName().getString() + "> ")
                        .append(Component.literal(getTranslate(SCREENSHOT, "Screenshot")).withStyle(style ->
                                style.withColor(CHAT_LINK_COLOR)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, message.getAttachments().getFirst().getUrl()))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(message.getAttachments().getFirst().getUrl())))
                        ))
        );
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
