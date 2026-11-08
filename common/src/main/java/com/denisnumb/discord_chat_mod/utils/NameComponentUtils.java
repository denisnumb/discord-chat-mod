package com.denisnumb.discord_chat_mod.utils;

import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class NameComponentUtils {
    private NameComponentUtils() {}

    public static MutableComponent player(@Nullable UUID uuid, String name) {
        return Component.literal(name).withStyle(style -> {
            style = style
                    .withClickEvent(new ClickEvent.SuggestCommand("/tell " + name + " "))
                    .withInsertion(name);

            return uuid == null
                    ? style
                    : style.withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, uuid, Component.literal(name))));
        });
    }

    public static MutableComponent player(String name) {
        return player(null, name);
    }

    public static MutableComponent discordMember(Member member) {
        return discordMember(member, ChannelMembersProvider.getMemberDisplayName(member));
    }

    public static MutableComponent discordMember(Member member, String text) {
        String displayName = ChannelMembersProvider.getMemberDisplayName(member);
        MutableComponent root = Component.empty().withStyle(style -> style
                .withInsertion("@" + displayName)
                .withClickEvent(new ClickEvent.SuggestCommand("/mention " + displayName))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(member.getUser().getName())))
        );

        int[] colors = ColorUtils.parseRoleColors(member.getColors());
        return root.append(ComponentUtils.buildGradientComponent(text, colors));
    }

    public static MutableComponent discordUser(User user) {
        return Component.literal(user.getEffectiveName()).withStyle(style -> style
                .withInsertion("@" + user.getEffectiveName())
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(user.getName())))
        );
    }
}
