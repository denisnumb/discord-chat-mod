package com.denisnumb.discord_chat_mod.chat_images.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PlayerSelectionList extends ObjectSelectionList<PlayerSelectionList.PlayerEntry> {
    private final List<PlayerInfo> allPlayers;
    private final List<String> selectedPlayers = new ArrayList<>();
    private final Consumer<List<String>> onSelectionChanged;

    public PlayerSelectionList(Minecraft minecraft, int width, int height, int x, int y, int entryHeight,
                                List<PlayerInfo> players,
                                Consumer<List<String>> onSelectionChanged) {
        super(minecraft, width, height, y, entryHeight);
        this.setX(x);
        this.allPlayers = players;
        this.onSelectionChanged = onSelectionChanged;
        setFilter("");
    }

    public List<String> getSelectedPlayers() {
        return selectedPlayers;
    }

    public void setFilter(String query) {
        String queryLowerCase = query.toLowerCase(Locale.ROOT);
        clearEntries();
        allPlayers.stream()
                .filter(p -> p.getProfile().name().toLowerCase(Locale.ROOT).contains(queryLowerCase))
                .sorted((a, b) -> a.getProfile().name().compareToIgnoreCase(b.getProfile().name()))
                .map(PlayerEntry::new)
                .forEach(this::addEntry);

        setScrollAmount(0);
    }

    @Override
    public int getRowWidth() {
        return this.width - 8;
    }

    public class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {
        private final String name;
        private final Checkbox checkbox;

        public PlayerEntry(PlayerInfo playerInfo) {
            this.name = playerInfo.getProfile().name();
            this.checkbox = Checkbox.builder(Component.literal(name), Minecraft.getInstance().font)
                    .selected(selectedPlayers.contains(name))
                    .onValueChange((cb, value) -> {
                        if (value) selectedPlayers.add(name);
                        else selectedPlayers.remove(name);
                        onSelectionChanged.accept(selectedPlayers);
                    })
                    .build();
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean bl) {
            return checkbox.mouseClicked(mouseButtonEvent, bl);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(name);
        }

        @Override
        public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            checkbox.setPosition(getContentX(), getY() + (getHeight() - checkbox.getHeight()) / 2);
            checkbox.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}