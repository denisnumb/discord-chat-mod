package com.denisnumb.discord_chat_mod.discord.model;

import com.denisnumb.discord_chat_mod.ColorUtils;

import java.awt.Color;

public class DiscordUserData {
    public String displayName;
    public String discordName;
    public String prettyMention;
    public String mentionString;
    public String color;

    public DiscordUserData(
            String displayName,
            String discordName,
            String mentionString,
            Color color
    ){
        this.displayName = displayName;
        this.discordName = discordName;
        this.prettyMention = "@" + displayName;
        this.mentionString = mentionString;
        this.color = ColorUtils.getHexColor(color);
    }

    public String getPrettyMention() {
        return prettyMention;
    }
}
