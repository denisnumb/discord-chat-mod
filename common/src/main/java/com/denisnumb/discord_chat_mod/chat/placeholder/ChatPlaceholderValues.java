package com.denisnumb.discord_chat_mod.chat.placeholder;

import com.denisnumb.discord_chat_mod.utils.ItemDisplayUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The values of the {@link ChatPlaceholder}s for a message, captured on the server thread when the message is sent:
 * the Discord message is formatted later on another thread, when the player might already have other properties
 */
public record ChatPlaceholderValues(ItemStack heldItem) {
    public static ChatPlaceholderValues capture(@Nullable Entity sender) {
        return new ChatPlaceholderValues(ItemDisplayUtils.getDisplayedItem(sender));
    }

    public Map<ChatPlaceholder, Component> forMinecraft(Set<ChatPlaceholder> placeholders) {
        Map<ChatPlaceholder, Component> result = new EnumMap<>(ChatPlaceholder.class);

        for (ChatPlaceholder placeholder : placeholders) {
            Component value = switch (placeholder) {
                case ITEM -> heldItem.isEmpty() ? null : ItemDisplayUtils.getHoverableName(heldItem);
            };

            if (value != null)
                result.put(placeholder, value);
        }

        return result;
    }
}
