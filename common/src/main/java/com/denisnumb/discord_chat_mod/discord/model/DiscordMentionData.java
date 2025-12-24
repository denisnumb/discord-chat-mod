package com.denisnumb.discord_chat_mod.discord.model;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHANNEL_MENTION_COLOR;
import static com.denisnumb.discord_chat_mod.ColorUtils.getHexColor;
import static com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider.getMemberDisplayName;

public class DiscordMentionData {
    public DiscordUserData memberData;
    public String prettyMention;
    public String color;

    public DiscordMentionData(Member member){
        this(new DiscordUserData(
                getMemberDisplayName(member),
                member.getUser().getName(),
                member.getAsMention(),
                member.getColor()
        ));
    }

    public DiscordMentionData(Role role){
        this("@" + role.getName(), getHexColor(role.getColor()));
    }

    public DiscordMentionData(GuildChannel channel){
        this("#" + channel.getName(), getHexColor(CHANNEL_MENTION_COLOR));
    }

    public DiscordMentionData(DiscordUserData memberData){
        this(memberData.prettyMention, memberData.color);
        this.memberData = memberData;
    }

    public DiscordMentionData(String prettyMention, String color){
        this.prettyMention = prettyMention;
        this.color = color;
    }
}
