package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.network.ModNetworking;
import com.denisnumb.discord_chat_mod.network.emoji.RequestDiscordEmojisPacket;
import com.denisnumb.discord_chat_mod.network.mentions.RequestDiscordMentionsPacket;
import com.vdurmont.emoji.EmojiParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.File;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.LIME;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslateClient;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.CLICK_TO_SEND_SCREENSHOT;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.CLICK_TO_SEND_SCREENSHOT_HINT;

@Mod.EventBusSubscriber(modid = DiscordChatMod.MODID, value = Dist.CLIENT)
public class MinecraftClientEvents {
    @SubscribeEvent
    public static void onScreenshot(ScreenshotEvent event){
        File screenshotFile = event.getScreenshotFile();

        Component screenshotName = Component.literal(screenshotFile.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsolutePath()))
                );

        Component clickToSendComponent = Component.literal(" " + getTranslateClient(CLICK_TO_SEND_SCREENSHOT, "[Send to Discord]")).withStyle(style ->
                style.withColor(LIME)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "send_screenshot " + screenshotFile.getAbsolutePath()
                        )).withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal(
                                        getTranslateClient(
                                                CLICK_TO_SEND_SCREENSHOT_HINT,
                                                "Click to send screenshot to Discord (Sending may take a few seconds)\n\n*Hold Shift to send screenshot as spoiler"
                                        )
                                )
                        ))
        );

        event.setResultMessage(
                Component.literal(getTranslateClient("screenshot.success").replace("%s", ""))
                        .append(screenshotName)
                        .append(clickToSendComponent)
        );
    }

    @SubscribeEvent
    public static void onChatMessage(ClientChatReceivedEvent event){
        if (Config.EMOJIFUL_COMPATIBILITY.get()){
            if (!EmojiParser.extractEmojis(event.getMessage().getString()).isEmpty()){
                MutableComponent withReplacedEmojis = Component.empty();
                for (Component comp : event.getMessage().toFlatList())
                    withReplacedEmojis.append(Component.literal(EmojiParser.parseToAliases(comp.getString())).withStyle(comp.getStyle()));

                event.setMessage(withReplacedEmojis);
            }
        }
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event){
        ModNetworking.sendToServer(new RequestDiscordMentionsPacket());
        ModNetworking.sendToServer(new RequestDiscordEmojisPacket());
    }
}
