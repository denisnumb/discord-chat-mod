package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;

import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsTransceiver;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersTransceiver;
import com.vdurmont.emoji.EmojiParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.io.File;

import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslateClient;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;

public class MinecraftClientEvents {
    public static Component handleScreenshot(File screenshotFile){
        Component screenshotName = Component.literal(screenshotFile.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style ->
                        style.withClickEvent(new ClickEvent.OpenFile(screenshotFile.getAbsolutePath()))
                );

        Component clickToSendComponent = Component.literal(" " + getTranslateClient(CLICK_TO_SEND_SCREENSHOT)).withStyle(style ->
                style.withColor(ChatFormatting.GREEN.getColor())
                        .withClickEvent(new ClickEvent.RunCommand("send_screenshot " + screenshotFile.getAbsolutePath()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(getTranslateClient(CLICK_TO_SEND_SCREENSHOT_HINT))))
        );

        return Component.literal(getTranslateClient("screenshot.success").replace("%s", ""))
                .append(screenshotName)
                .append(clickToSendComponent);
    }

    public static Component handleChatMessage(Component message){
        if (ConfigProvider.getConfig().isEmojifulCompatibilityEnabled()){
            if (!EmojiParser.extractEmojis(message.getString()).isEmpty()){
                MutableComponent withReplacedEmojis = Component.empty();
                for (Component comp : message.toFlatList())
                    withReplacedEmojis.append(Component.literal(EmojiParser.parseToAliases(comp.getString())).withStyle(comp.getStyle()));

                return withReplacedEmojis;
            }
        }

        return message;
    }

    public static void handleJoinServer(){
        DiscordMentionsTransceiver.requestDiscordMemberData();
        DiscordEmojisTransceiver.requestDiscordEmojis();
        DiscordStickersTransceiver.requestDiscordStickers();
    }
}
