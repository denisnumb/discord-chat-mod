package com.denisnumb.discord_chat_mod.network.screenshot;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftUtils;
import com.denisnumb.discord_chat_mod.chat_images.ImageStorage;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import com.google.gson.Gson;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslateClient;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.sendMessageToAllPlayersFromPlayer;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.LOCAL_RESOURCE_PREFIX;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.SPOILER_PREFIX;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;
import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;

public class ScreenshotTransceiver {
    private static final Gson gson = new Gson();
    private static final ExecutorService networkPool = Executors.newSingleThreadExecutor();
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static long lastSendScreenshotTime = 0;
    private static File lastFile = null;

    public static void sendScreenshotToServer(File screenshotFile, Player player, boolean sendAsSpoiler) {
        long currentTime = System.currentTimeMillis();

        if ((screenshotFile.equals(lastFile) && currentTime - lastSendScreenshotTime < 20000)
                || (currentTime - lastSendScreenshotTime < 2000)
        ) {
            MinecraftUtils.showTitleBarMessage(Component.literal(
                    getTranslateClient(SCREENSHOT_COOLDOWN)
        ).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            return;
        }

        MinecraftUtils.showTitleBarMessage(Component.literal(getTranslateClient(SENDING_SCREENSHOT)));
        networkPool.execute(() -> {
            try {
                byte[] screenshotBytes = Files.readAllBytes(screenshotFile.toPath());
                String fileName = sendAsSpoiler
                        ? SPOILER_PREFIX + screenshotFile.getName()
                        : screenshotFile.getName();
                byte[] data = gson.toJson(new ImageData(fileName, screenshotBytes)).getBytes();
                lastFile = screenshotFile;
                lastSendScreenshotTime = currentTime;

                BigPacketsTransceiver.send(data, (partIndex, totalParts, part) ->
                        PlatformPacketDistributor.sendToServer(new ScreenshotPartPacketServer(currentTime, partIndex, totalParts, part))
                );
            } catch (Exception e) {
                sendErrorMessageToPlayer(player, e.getMessage());
            }
        });
    }

    public static void receivePartClientSide(ScreenshotPartPacketClient packet) {
        receivePartCommon(packet, ScreenshotTransceiver::handleReceivedScreenshotClient);
    }

    public static void receivePartServerSide(ScreenshotPartPacketServer packet, ServerPlayer player) {
        receivePartCommon(packet, rawData -> handleReceivedScreenshotServer(rawData, player));
    }

    private static void receivePartCommon(BasePartPacket packet, Consumer<byte[]> onComplete) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.sendTime,
                packet.partIndex,
                packet.totalParts,
                packet.data
        ).ifPresent(rawData -> networkPool.execute(() -> onComplete.accept(rawData)));
    }

    private static void handleReceivedScreenshotClient(byte[] rawScreenshotData) {
        ImageData screenshotData = gson.fromJson(new String(rawScreenshotData), ImageData.class);
        try {
            ImageStorage.registerImage(screenshotData.fileName(), screenshotData.data());
        } catch (Exception e) {
            DiscordChatMod.LOGGER.error("ClientScreenshotHandleError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleReceivedScreenshotServer(byte[] rawScreenshotData, ServerPlayer player) {
        if (isDiscordConnected()) {
            sendScreenshotToDiscord(rawScreenshotData, player);
            return;
        }

        sendScreenshotToPlayers(rawScreenshotData, player);
    }

    private static void sendScreenshotToPlayers(byte[] rawScreenshotData, ServerPlayer fromPlayer) {
        ImageData screenshotData = gson.fromJson(new String(rawScreenshotData), ImageData.class);
        long currentTime = System.currentTimeMillis();
        String uniqueFilename = LOCAL_RESOURCE_PREFIX + currentTime + "/" + screenshotData.fileName();

        networkPool.execute(() -> {
            try {
                byte[] data = gson.toJson(new ImageData(uniqueFilename, screenshotData.data())).getBytes();
                List<ServerPlayer> playerList = Objects.requireNonNull(fromPlayer.getServer()).getPlayerList().getPlayers();

                for (ServerPlayer player : playerList) {
                    try {
                        BigPacketsTransceiver.send(data, (partIndex, totalParts, part) ->
                                PlatformPacketDistributor.sendToPlayer(player, new ScreenshotPartPacketClient(currentTime, partIndex, totalParts, part))
                        );
                    } catch (Exception ignored) {
                    }
                }

                Component screenshotComponent = Component.literal(getTranslate(SCREENSHOT)).withStyle(style ->
                        style.withColor(CHAT_LINK_COLOR).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, uniqueFilename))
                );
                sendMessageToAllPlayersFromPlayer(Map.of(PLAYER, fromPlayer.getDisplayName(), MESSAGE, screenshotComponent));
            } catch (Exception e) {
                sendErrorMessageToPlayer(fromPlayer, e.getMessage());
            }
        });
    }

    private static void sendScreenshotToDiscord(byte[] rawScreenshotData, ServerPlayer player) {
        ImageData screenshotData = gson.fromJson(new String(rawScreenshotData), ImageData.class);

        Map<String, String> parameters = mergeMaps(
                Map.of(SCREENSHOT_URL, String.format("attachment://%s", screenshotData.fileName())),
                buildPlayerParameters(player)
        );

        Optional<DiscordMessageComponents> chatComponentsOpt = getDiscordMessageComponents(MessageType.SCREENSHOT, parameters);
        Optional<DiscordMessageComponents> webhookComponentsOpt = getDiscordMessageComponents(MessageType.SCREENSHOT_WEBHOOK, parameters);

        if (chatComponentsOpt.isPresent() && webhookComponentsOpt.isPresent()) {
            sendMessageFromPlayer(ChannelCategory.SCREENSHOTS, getAllContexts(), player, webhookComponentsOpt.get(), chatComponentsOpt.get(), screenshotData)
                    .ifPresentOrElse(
                            screenshotUrl -> handleSuccessfulDiscordSend(screenshotUrl, player),
                            () -> sendScreenshotToPlayers(rawScreenshotData, player)
                    );
        }
    }

    private static void handleSuccessfulDiscordSend(String screenshotUrl, Player player) {
        Component screenshotComponent = Component.literal(getTranslate(SCREENSHOT))
                .withStyle(style -> style.withColor(CHAT_LINK_COLOR)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, screenshotUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(screenshotUrl)))
                );

        sendMessageToAllPlayersFromPlayer(Map.of(PLAYER, player.getDisplayName(), MESSAGE, screenshotComponent));
    }

    private static void sendErrorMessageToPlayer(Player player, String errorMessage) {
        String message = String.format(getTranslate(SCREENSHOT_SENDING_ERROR_WITH_REASON), errorMessage);
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }
}
