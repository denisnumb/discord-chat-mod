package com.denisnumb.discord_chat_mod.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ItemDisplayUtils {
    private ItemDisplayUtils() {}

    public static ItemStack getDisplayedItem(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity))
            return ItemStack.EMPTY;

        ItemStack stack = livingEntity.getMainHandItem().isEmpty()
                ? livingEntity.getOffhandItem()
                : livingEntity.getMainHandItem();

        return stack.copy();
    }

    public static Component getHoverableName(ItemStack stack) {
        return stack.getDisplayName();
    }

    public static List<Component> getEnchantmentNames(ItemStack stack, RegistryAccess registryAccess) {
        HolderSet<@NotNull Enchantment> tooltipOrder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.TOOLTIP_ORDER)
                .<HolderSet<@NotNull Enchantment>>map(tag -> tag)
                .orElse(HolderSet.empty());

        TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        List<Component> result = new ArrayList<>();

        for (DataComponentType<@NotNull ItemEnchantments> type : List.of(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS)) {
            if (!tooltipDisplay.shows(type))
                continue;

            ItemEnchantments enchantments = stack.getOrDefault(type, ItemEnchantments.EMPTY);
            if (enchantments.isEmpty())
                continue;

            for (Holder<@NotNull Enchantment> enchantment : tooltipOrder) {
                int level = enchantments.getLevel(enchantment);
                if (level > 0)
                    result.add(Enchantment.getFullname(enchantment, level));
            }

            for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> entry : enchantments.entrySet()) {
                if (!tooltipOrder.contains(entry.getKey()))
                    result.add(Enchantment.getFullname(entry.getKey(), entry.getIntValue()));
            }
        }

        return result;
    }
}
