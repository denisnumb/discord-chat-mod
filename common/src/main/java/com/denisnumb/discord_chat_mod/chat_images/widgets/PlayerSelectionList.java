package com.denisnumb.discord_chat_mod.chat_images.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
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
                .filter(p -> p.getProfile().getName().toLowerCase(Locale.ROOT).contains(queryLowerCase))
                .sorted((a, b) -> a.getProfile().getName().compareToIgnoreCase(b.getProfile().getName()))
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
        public static final int CONTENT_PADDING = 2;

        public PlayerEntry(PlayerInfo playerInfo) {
            this.name = playerInfo.getProfile().getName();
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
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return checkbox.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(name);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            checkbox.setPosition(left + CONTENT_PADDING, top + (height - checkbox.getHeight()) / 2);
            checkbox.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}