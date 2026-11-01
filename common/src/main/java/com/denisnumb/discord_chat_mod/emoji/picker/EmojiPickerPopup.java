package com.denisnumb.discord_chat_mod.emoji.picker;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.emoji.UnicodeEmojis;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersTransceiver;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmojiPickerPopup {
    public enum Tab {EMOJI, STICKERS}

    public interface Host {
        void addListener(EditBox listener);

        void removeListener(GuiEventListener listener);

        void focus(GuiEventListener listener);

        void onEmojiSelected(PickerEntry entry, boolean keepOpen);

        void onStickerSelected(PickerEntry.Sticker sticker);

        static Host of(
                Consumer<EditBox> addListener,
                Consumer<GuiEventListener> removeListener,
                Consumer<GuiEventListener> focus,
                BiConsumer<PickerEntry, Boolean> onEmojiSelected,
                Consumer<PickerEntry.Sticker> onStickerSelected
        ) {
            return new Host() {
                @Override
                public void addListener(EditBox listener) {
                    addListener.accept(listener);
                }

                @Override
                public void removeListener(GuiEventListener listener) {
                    removeListener.accept(listener);
                }

                @Override
                public void focus(GuiEventListener listener) {
                    focus.accept(listener);
                }

                @Override
                public void onEmojiSelected(PickerEntry entry, boolean keepOpen) {
                    onEmojiSelected.accept(entry, keepOpen);
                }

                @Override
                public void onStickerSelected(PickerEntry.Sticker sticker) {
                    onStickerSelected.accept(sticker);
                }
            };
        }
    }

    private static final int BORDER = 1;
    private static final int PADDING = 4;
    private static final int EMOJI_CELL = 18;
    private static final int EMOJI_COLUMNS = 9;
    private static final int STICKER_CELL = 52;
    private static final int STICKER_COLUMNS = 3;
    private static final int STICKER_GAP = 3;
    private static final int GRID_WIDTH = EMOJI_CELL * EMOJI_COLUMNS;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_GAP = 2;
    public static final int WIDTH = BORDER + PADDING + GRID_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH + PADDING + BORDER;
    private static final int PREFERRED_HEIGHT = 214;
    private static final int MIN_HEIGHT = 110;
    private static final int TAB_HEIGHT = 14;
    private static final int SEARCH_HEIGHT = 14;
    private static final int CATEGORY_SIZE = 16;
    private static final int FOOTER_HEIGHT = 18;
    private static final int SECTION_HEADER_HEIGHT = 11;
    private static final int SECTION_GAP = 3;
    private static final int CUSTOM_EMOJI_SIZE = 14;
    private static final float GRID_EMOJI_SCALE = 1.5f;
    private static final double SCROLL_STEP = EMOJI_CELL * 2;

    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_BACKGROUND = 0xF01A1A1A;
    private static final int COLOR_SEPARATOR = 0x33FFFFFF;
    private static final int COLOR_HOVER = 0x30FFFFFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFA0A0A0;
    private static final int COLOR_SCROLL_TRACK = 0x40000000;
    private static final int COLOR_SCROLL_THUMB = 0xFFA0A0A0;
    private static final int COLOR_PLACEHOLDER = 0x20FFFFFF;

    private static final String RECENT_ANCHOR = "recent";
    private static final String CUSTOM_ANCHOR = "custom";
    private static final String RECENT_ICON = "🕘";
    private static final String CUSTOM_FALLBACK_ICON = "★";

    private static Tab lastTab = Tab.EMOJI;

    private record Section(Component title, List<PickerEntry> entries, int cellSize, int columns, int gap,
                           @Nullable Object anchor) {
        int rows() {
            return (entries.size() + columns - 1) / columns;
        }

        int height() {
            return SECTION_HEADER_HEIGHT + rows() * (cellSize + gap) - gap + SECTION_GAP;
        }
    }

    private record Category(Object anchor, int sectionIndex, Component title, @Nullable String iconText,
                            @Nullable PickerEntry.CustomEmoji iconEmoji) {
    }

    private final Font font;
    private final Host host;

    private boolean visible = false;
    private Tab tab = lastTab;
    private int x, y, height;
    private EditBox searchBox;
    private String query = "";

    private List<PickerEntry> recent = List.of();
    private List<Section> sections = List.of();
    private List<Category> categories = List.of();
    private int[] sectionTops = new int[0];
    private int contentHeight = 0;
    private double scroll = 0;
    private int lastCustomCount = -1;
    private int lastStickerCount = -1;

    @Nullable
    private PickerEntry hoveredEntry;
    @Nullable
    private Component hoveredHint;

    public EmojiPickerPopup(Minecraft minecraft, Host host) {
        this.font = minecraft.font;
        this.host = host;
    }

    public boolean isVisible() {
        return visible;
    }

    public Tab getTab() {
        return tab;
    }

    public void focusSearch() {
        if (visible)
            host.focus(searchBox);
    }

    public void show(int rightX, int bottomY) {
        String previousQuery = visible ? query : "";
        hideInternal();

        height = Mth.clamp(bottomY - 4, MIN_HEIGHT, PREFERRED_HEIGHT);
        x = Math.max(2, rightX - WIDTH);
        y = bottomY - height;

        searchBox = new EditBox(font, searchX(), searchY(), searchWidth(), SEARCH_HEIGHT, Component.empty());
        searchBox.setMaxLength(64);
        searchBox.setHint(searchHint());
        searchBox.setValue(previousQuery);
        searchBox.setResponder(this::onQueryChanged);
        host.addListener(searchBox);
        host.focus(searchBox);

        query = previousQuery;
        visible = true;
        recent = RecentEmojiStore.entries();

        DiscordEmojisTransceiver.requestDiscordEmojis();
        DiscordStickersTransceiver.requestDiscordStickers();

        rebuild();
    }

    public void relayout(int rightX, int bottomY) {
        if (visible)
            show(rightX, bottomY);
    }

    public void hide() {
        hideInternal();
        query = "";
        scroll = 0;
    }

    private void hideInternal() {
        if (!visible)
            return;
        host.removeListener(searchBox);
        searchBox = null;
        visible = false;
        hoveredEntry = null;
        hoveredHint = null;
    }

    private void setTab(Tab newTab) {
        if (tab == newTab)
            return;
        tab = newTab;
        lastTab = newTab;
        scroll = 0;
        searchBox.setHint(searchHint());
        searchBox.setValue("");
        query = "";
        rebuild();
        host.focus(searchBox);
    }

    private void onQueryChanged(String value) {
        if (value.equals(query))
            return;
        query = value;
        scroll = 0;
        rebuild();
    }

    private Component searchHint() {
        return tab == Tab.EMOJI
                ? MinecraftLocaleProvider.EmojiPicker.Search.emoji()
                : MinecraftLocaleProvider.EmojiPicker.Search.stickers();
    }

    private void rebuild() {
        List<Section> newSections = new ArrayList<>();
        boolean searching = !EmojiCatalog.normalizeQuery(query).isEmpty();

        if (tab == Tab.EMOJI) {
            if (!searching) {
                if (!recent.isEmpty())
                    newSections.add(emojiSection(MinecraftLocaleProvider.EmojiPicker.Section.recent(), recent, RECENT_ANCHOR));

                List<PickerEntry> custom = new ArrayList<>(EmojiCatalog.customEmojis());
                if (!custom.isEmpty())
                    newSections.add(emojiSection(MinecraftLocaleProvider.EmojiPicker.Section.server(), custom, CUSTOM_ANCHOR));

                for (EmojiCatalog.UnicodeGroup group : EmojiCatalog.unicodeGroups())
                    newSections.add(emojiSection(group.group().title(), new ArrayList<>(group.entries()), group.group()));
            } else {
                List<PickerEntry> custom = new ArrayList<>(EmojiCatalog.searchCustom(query));
                if (!custom.isEmpty())
                    newSections.add(emojiSection(MinecraftLocaleProvider.EmojiPicker.Section.server(), custom, null));

                List<PickerEntry> unicode = new ArrayList<>(EmojiCatalog.searchUnicode(query));
                if (!unicode.isEmpty())
                    newSections.add(emojiSection(MinecraftLocaleProvider.EmojiPicker.Section.standard(), unicode, null));
            }
        } else {
            if (!searching) {
                for (Map.Entry<String, List<PickerEntry.Sticker>> guild : EmojiCatalog.stickersByGuild().entrySet()) {
                    Component title = guild.getKey().isEmpty()
                            ? MinecraftLocaleProvider.EmojiPicker.Tab.stickers()
                            : Component.literal(guild.getKey());
                    newSections.add(stickerSection(title, new ArrayList<>(guild.getValue())));
                }
            } else {
                List<PickerEntry> found = new ArrayList<>(EmojiCatalog.searchStickers(query));
                if (!found.isEmpty())
                    newSections.add(stickerSection(MinecraftLocaleProvider.EmojiPicker.Tab.stickers(), found));
            }
        }

        sections = List.copyOf(newSections);
        sectionTops = new int[sections.size()];
        int acc = 0;
        for (int i = 0; i < sections.size(); i++) {
            sectionTops[i] = acc;
            acc += sections.get(i).height();
        }
        contentHeight = acc;

        List<Category> newCategories = new ArrayList<>();
        if (tab == Tab.EMOJI && !searching) {
            for (int i = 0; i < sections.size(); i++) {
                Section section = sections.get(i);
                if (RECENT_ANCHOR.equals(section.anchor())) {
                    newCategories.add(new Category(RECENT_ANCHOR, i, section.title(), UnicodeEmojis.glyphFor(RECENT_ICON), null));
                } else if (CUSTOM_ANCHOR.equals(section.anchor())) {
                    PickerEntry first = section.entries().getFirst();
                    newCategories.add(new Category(CUSTOM_ANCHOR, i, section.title(), CUSTOM_FALLBACK_ICON,
                            first instanceof PickerEntry.CustomEmoji custom ? custom : null));
                } else if (section.anchor() instanceof EmojiCatalog.Group group) {
                    newCategories.add(new Category(group, i, section.title(), group.iconGlyph(), null));
                }
            }
        }
        categories = List.copyOf(newCategories);

        lastCustomCount = EmojiCatalog.customEmojiCount();
        lastStickerCount = EmojiCatalog.stickerCount();
        clampScroll();
    }

    private static Section emojiSection(Component title, List<PickerEntry> entries, @Nullable Object anchor) {
        return new Section(title, entries, EMOJI_CELL, EMOJI_COLUMNS, 0, anchor);
    }

    private static Section stickerSection(Component title, List<PickerEntry> entries) {
        return new Section(title, entries, STICKER_CELL, STICKER_COLUMNS, STICKER_GAP, null);
    }

    private void refreshIfDataChanged() {
        if (EmojiCatalog.customEmojiCount() != lastCustomCount || EmojiCatalog.stickerCount() != lastStickerCount)
            rebuild();
    }

    private int innerX() {
        return x + BORDER;
    }

    private int innerRight() {
        return x + WIDTH - BORDER;
    }

    private int tabsY() {
        return y + BORDER;
    }

    private int searchX() {
        return x + BORDER + PADDING;
    }

    private int searchY() {
        return tabsY() + TAB_HEIGHT + 3;
    }

    private int searchWidth() {
        return WIDTH - 2 * (BORDER + PADDING);
    }

    private boolean hasCategories() {
        return !categories.isEmpty();
    }

    private int categoriesY() {
        return searchY() + SEARCH_HEIGHT + 3;
    }

    private int gridX() {
        return x + BORDER + PADDING;
    }

    private int gridY() {
        return hasCategories() ? categoriesY() + CATEGORY_SIZE + 3 : searchY() + SEARCH_HEIGHT + 4;
    }

    private int footerY() {
        return y + height - BORDER - FOOTER_HEIGHT;
    }

    private int gridHeight() {
        return Math.max(0, footerY() - 2 - gridY());
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - gridHeight());
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    public boolean contains(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + height;
    }

    private static boolean inRect(double px, double py, int rx, int ry, int rw, int rh) {
        return px >= rx && px < rx + rw && py >= ry && py < ry + rh;
    }

    private int cellX(Section section, int index) {
        return gridX() + (index % section.columns()) * (section.cellSize() + section.gap());
    }

    private int cellY(int sectionIndex, Section section, int index) {
        return gridY() - (int) scroll + sectionTops[sectionIndex] + SECTION_HEADER_HEIGHT
                + (index / section.columns()) * (section.cellSize() + section.gap());
    }

    @Nullable
    private PickerEntry entryAt(double mouseX, double mouseY) {
        if (!inRect(mouseX, mouseY, gridX(), gridY(), GRID_WIDTH, gridHeight()))
            return null;

        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            int top = gridY() - (int) scroll + sectionTops[i] + SECTION_HEADER_HEIGHT;
            if (mouseY < top || mouseY >= top + section.rows() * (section.cellSize() + section.gap()))
                continue;

            int step = section.cellSize() + section.gap();
            int col = (int) ((mouseX - gridX()) / step);
            int row = (int) ((mouseY - top) / step);
            if (col >= section.columns())
                return null;
            if ((mouseX - gridX()) - col * step >= section.cellSize() || (mouseY - top) - row * step >= section.cellSize())
                return null;

            int index = row * section.columns() + col;
            return index < section.entries().size() ? section.entries().get(index) : null;
        }
        return null;
    }

    @Nullable
    private Category categoryAt(double mouseX, double mouseY) {
        if (!hasCategories() || !inRect(mouseX, mouseY, gridX(), categoriesY(), categories.size() * CATEGORY_SIZE, CATEGORY_SIZE))
            return null;
        int index = (int) ((mouseX - gridX()) / CATEGORY_SIZE);
        return index >= 0 && index < categories.size() ? categories.get(index) : null;
    }

    @Nullable
    private Tab tabAt(double mouseX, double mouseY) {
        if (!inRect(mouseX, mouseY, innerX(), tabsY(), innerRight() - innerX(), TAB_HEIGHT))
            return null;
        return mouseX < innerX() + (innerRight() - innerX()) / 2.0 ? Tab.EMOJI : Tab.STICKERS;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible)
            return;

        refreshIfDataChanged();
        hoveredEntry = null;
        hoveredHint = null;

        graphics.nextStratum();
        graphics.fill(x, y, x + WIDTH, y + height, COLOR_BORDER);
        graphics.fill(x + BORDER, y + BORDER, x + WIDTH - BORDER, y + height - BORDER, COLOR_BACKGROUND);

        renderTabs(graphics, mouseX, mouseY);
        searchBox.render(graphics, mouseX, mouseY, partialTick);
        renderCategories(graphics, mouseX, mouseY);
        renderGrid(graphics, mouseX, mouseY);
        renderFooter(graphics);
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = innerX();
        int half = (innerRight() - innerX()) / 2;
        Tab hovered = tabAt(mouseX, mouseY);

        renderTab(graphics, Tab.EMOJI, MinecraftLocaleProvider.EmojiPicker.Tab.emoji(), left, half, hovered == Tab.EMOJI);
        renderTab(graphics, Tab.STICKERS, MinecraftLocaleProvider.EmojiPicker.Tab.stickers(), left + half, innerRight() - left - half, hovered == Tab.STICKERS);

        graphics.fill(innerX(), tabsY() + TAB_HEIGHT, innerRight(), tabsY() + TAB_HEIGHT + 1, COLOR_SEPARATOR);
    }

    private void renderTab(GuiGraphics graphics, Tab value, Component title, int tabX, int tabWidth, boolean hovered) {
        boolean active = tab == value;
        if (hovered && !active) {
            graphics.fill(tabX, tabsY(), tabX + tabWidth, tabsY() + TAB_HEIGHT, COLOR_HOVER);
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        int textWidth = font.width(title);
        graphics.drawString(font, title, tabX + (tabWidth - textWidth) / 2, tabsY() + (TAB_HEIGHT - 8) / 2,
                active ? COLOR_TEXT : COLOR_TEXT_MUTED, active);

        if (active)
            graphics.fill(tabX + 4, tabsY() + TAB_HEIGHT - 1, tabX + tabWidth - 4, tabsY() + TAB_HEIGHT, COLOR_TEXT);
    }

    private void renderCategories(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!hasCategories())
            return;

        Category hovered = categoryAt(mouseX, mouseY);
        int activeSection = currentSectionIndex();

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            int bx = gridX() + i * CATEGORY_SIZE;
            int by = categoriesY();

            if (category == hovered) {
                graphics.fill(bx, by, bx + CATEGORY_SIZE, by + CATEGORY_SIZE, COLOR_HOVER);
                graphics.requestCursor(CursorTypes.POINTING_HAND);
                hoveredHint = category.title();
            }

            AbstractImage image = category.iconEmoji() != null ? category.iconEmoji().image() : null;
            if (!drawImageFit(graphics, image, bx + 3, by + 3, CATEGORY_SIZE - 6, CATEGORY_SIZE - 6) && category.iconText() != null)
                drawEmojiText(graphics, category.iconText(), bx, by, CATEGORY_SIZE, 1.0f);

            if (category.sectionIndex() == activeSection)
                graphics.fill(bx + 2, by + CATEGORY_SIZE - 1, bx + CATEGORY_SIZE - 2, by + CATEGORY_SIZE, COLOR_TEXT);
        }

        int lineY = categoriesY() + CATEGORY_SIZE + 1;
        graphics.fill(innerX(), lineY, innerRight(), lineY + 1, COLOR_SEPARATOR);
    }

    private int currentSectionIndex() {
        int current = 0;
        for (int i = 0; i < sectionTops.length; i++) {
            if (sectionTops[i] <= scroll + 1)
                current = i;
        }
        return current;
    }

    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int gx = gridX();
        int gy = gridY();
        int gh = gridHeight();

        if (sections.isEmpty()) {
            renderEmptyState(graphics, gx, gy, GRID_WIDTH, gh);
            return;
        }

        PickerEntry hovered = entryAt(mouseX, mouseY);

        graphics.enableScissor(gx, gy, gx + GRID_WIDTH, gy + gh);
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            int top = gy - (int) scroll + sectionTops[i];
            if (top > gy + gh)
                break;
            if (top + section.height() < gy)
                continue;

            graphics.drawString(font, section.title(), gx + 1, top + 2, COLOR_TEXT_MUTED, false);

            for (int j = 0; j < section.entries().size(); j++) {
                int cy = cellY(i, section, j);
                if (cy > gy + gh)
                    break;
                if (cy + section.cellSize() < gy)
                    continue;

                int cx = cellX(section, j);
                PickerEntry entry = section.entries().get(j);

                if (entry == hovered) {
                    graphics.fill(cx, cy, cx + section.cellSize(), cy + section.cellSize(), COLOR_HOVER);
                    graphics.requestCursor(CursorTypes.POINTING_HAND);
                    hoveredEntry = entry;
                }
                renderEntry(graphics, entry, cx, cy, section.cellSize());
            }
        }
        graphics.disableScissor();

        renderScrollbar(graphics, gx + GRID_WIDTH + SCROLLBAR_GAP, gy, gh);
    }

    private void renderEntry(GuiGraphics graphics, PickerEntry entry, int cx, int cy, int cellSize) {
        switch (entry) {
            case PickerEntry.UnicodeEmoji unicode ->
                    drawEmojiText(graphics, unicode.glyph(), cx, cy, cellSize, GRID_EMOJI_SCALE);
            case PickerEntry.CustomEmoji custom -> {
                int offset = (cellSize - CUSTOM_EMOJI_SIZE) / 2;
                if (!drawImageFit(graphics, custom.image(), cx + offset, cy + offset, CUSTOM_EMOJI_SIZE, CUSTOM_EMOJI_SIZE))
                    graphics.fill(cx + offset, cy + offset, cx + offset + CUSTOM_EMOJI_SIZE, cy + offset + CUSTOM_EMOJI_SIZE, COLOR_PLACEHOLDER);
            }
            case PickerEntry.Sticker sticker -> {
                if (!drawImageFit(graphics, sticker.image(), cx + 2, cy + 2, cellSize - 4, cellSize - 4)) {
                    graphics.fill(cx + 2, cy + 2, cx + cellSize - 2, cy + cellSize - 2, COLOR_PLACEHOLDER);
                    String name = font.plainSubstrByWidth(sticker.name(), cellSize - 6);
                    graphics.drawString(font, name, cx + (cellSize - font.width(name)) / 2, cy + (cellSize - 8) / 2, COLOR_TEXT_MUTED, false);
                }
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int sx, int sy, int sh) {
        if (contentHeight <= sh || sh <= 0)
            return;

        graphics.fill(sx, sy, sx + SCROLLBAR_WIDTH, sy + sh, COLOR_SCROLL_TRACK);
        int thumbHeight = Math.max(10, (int) ((long) sh * sh / contentHeight));
        int thumbY = sy + (int) ((sh - thumbHeight) * (scroll / maxScroll()));
        graphics.fill(sx, thumbY, sx + SCROLLBAR_WIDTH, thumbY + thumbHeight, COLOR_SCROLL_THUMB);
    }

    private void renderEmptyState(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
        boolean stickersUnavailable = tab == Tab.STICKERS
                && EmojiCatalog.stickerCount() == 0
                && EmojiCatalog.normalizeQuery(query).isEmpty();

        Component title = stickersUnavailable
                ? MinecraftLocaleProvider.EmojiPicker.StickersUnavailable.title()
                : MinecraftLocaleProvider.EmojiPicker.nothingFound();

        List<FormattedCharSequence> lines = new ArrayList<>(font.split(title, gw - 8));
        int titleLines = lines.size();
        if (stickersUnavailable)
            lines.addAll(font.split(MinecraftLocaleProvider.EmojiPicker.StickersUnavailable.description(), gw - 8));

        int lineHeight = font.lineHeight + 1;
        int startY = gy + (gh - lines.size() * lineHeight) / 2;
        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.drawString(font, line, gx + (gw - font.width(line)) / 2, startY + i * lineHeight,
                    i < titleLines ? COLOR_TEXT : COLOR_TEXT_MUTED, i < titleLines);
        }
    }

    private void renderFooter(GuiGraphics graphics) {
        int fy = footerY();
        graphics.fill(innerX(), fy, innerRight(), fy + 1, COLOR_SEPARATOR);

        int left = innerX() + PADDING;
        int right = innerRight() - PADDING;
        int textY = fy + (FOOTER_HEIGHT - 8) / 2 + 1;

        if (hoveredEntry == null) {
            if (hoveredHint != null) {
                graphics.drawString(font, font.plainSubstrByWidth(hoveredHint.getString(), right - left), left, textY, COLOR_TEXT, true);
            } else if (!sections.isEmpty()) {
                Component hint = tab == Tab.STICKERS
                        ? MinecraftLocaleProvider.EmojiPicker.Hint.sendSticker()
                        : MinecraftLocaleProvider.EmojiPicker.Hint.insertMultiple();
                graphics.drawString(font, font.plainSubstrByWidth(hint.getString(), right - left), left, textY, COLOR_TEXT_MUTED, false);
            }
            return;
        }

        int previewSize = 14;
        int previewY = fy + 1 + (FOOTER_HEIGHT - 1 - previewSize) / 2;
        switch (hoveredEntry) {
            case PickerEntry.UnicodeEmoji unicode ->
                    drawEmojiText(graphics, unicode.glyph(), left - 1, previewY - 1, previewSize + 2, 1.5f);
            case PickerEntry.CustomEmoji custom ->
                    drawImageFit(graphics, custom.image(), left, previewY, previewSize, previewSize);
            case PickerEntry.Sticker sticker ->
                    drawImageFit(graphics, sticker.image(), left, previewY, previewSize, previewSize);
        }

        int nameX = left + previewSize + 4;
        drawEntryName(graphics, hoveredEntry.displayName(), nameX, textY, right - nameX);
    }

    private void drawEntryName(GuiGraphics graphics, String text, int textX, int textY, int maxWidth) {
        if (text.length() <= 2 || !text.startsWith(":") || !text.endsWith(":")) {
            graphics.drawString(font, font.plainSubstrByWidth(text, maxWidth), textX, textY, COLOR_TEXT, true);
            return;
        }

        int colonWidth = font.width(":");
        String inner = font.plainSubstrByWidth(text.substring(1, text.length() - 1), Math.max(0, maxWidth - colonWidth * 2));
        graphics.drawString(font, ":", textX, textY, COLOR_TEXT, true);
        graphics.drawString(font, inner, textX + colonWidth, textY, COLOR_TEXT, true);
        graphics.drawString(font, ":", textX + colonWidth + font.width(inner), textY, COLOR_TEXT, true);
    }

    private void drawEmojiText(GuiGraphics graphics, String text, int boxX, int boxY, int boxSize, float scale) {
        float textWidth = font.width(text) * scale;
        float textHeight = 8 * scale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(boxX + (boxSize - textWidth) / 2f, boxY + (boxSize - textHeight) / 2f);
        graphics.pose().scale(scale, scale);
        graphics.drawString(font, text, 0, 0, COLOR_TEXT, false);
        graphics.pose().popMatrix();
    }

    private static boolean drawImageFit(GuiGraphics graphics, @Nullable AbstractImage image, int boxX, int boxY, int boxWidth, int boxHeight) {
        if (image == null)
            return false;
        Identifier frame = image.getRenderFrame();
        if (frame == null)
            return false;

        int srcWidth = Math.max(1, image.originalSize.width());
        int srcHeight = Math.max(1, image.originalSize.height());
        float scale = Math.min((float) boxWidth / srcWidth, (float) boxHeight / srcHeight);
        int w = Math.max(1, Math.round(srcWidth * scale));
        int h = Math.max(1, Math.round(srcHeight * scale));
        int dx = boxX + (boxWidth - w) / 2;
        int dy = boxY + (boxHeight - h) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, frame, dx, dy, 0, 0, w, h, w, h);
        return true;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!contains(event.x(), event.y()))
            return false;

        double mx = event.x();
        double my = event.y();

        Tab clickedTab = tabAt(mx, my);
        if (clickedTab != null) {
            setTab(clickedTab);
            return true;
        }

        if (searchBox.isMouseOver(mx, my)) {
            host.focus(searchBox);
            searchBox.mouseClicked(event, doubleClick);
            return true;
        }

        Category category = categoryAt(mx, my);
        if (category != null) {
            scroll = sectionTops[category.sectionIndex()];
            clampScroll();
            return true;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            PickerEntry entry = entryAt(mx, my);
            if (entry != null) {
                select(entry, event.hasShiftDown());
                return true;
            }
        }

        host.focus(searchBox);
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!contains(mouseX, mouseY))
            return false;
        scroll -= scrollY * SCROLL_STEP;
        clampScroll();
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        switch (event.key()) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                PickerEntry first = EmojiCatalog.normalizeQuery(query).isEmpty() ? null : firstEntry();
                if (first != null)
                    select(first, false);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> scroll -= SCROLL_STEP;
            case GLFW.GLFW_KEY_DOWN -> scroll += SCROLL_STEP;
            case GLFW.GLFW_KEY_PAGE_UP -> scroll -= gridHeight();
            case GLFW.GLFW_KEY_PAGE_DOWN -> scroll += gridHeight();
            default -> {
                searchBox.keyPressed(event);
                return true;
            }
        }
        clampScroll();
        return true;
    }

    @Nullable
    private PickerEntry firstEntry() {
        for (Section section : sections) {
            if (!section.entries().isEmpty())
                return section.entries().getFirst();
        }
        return null;
    }

    private void select(PickerEntry entry, boolean keepOpen) {
        if (entry instanceof PickerEntry.Sticker sticker) {
            host.onStickerSelected(sticker);
            return;
        }
        RecentEmojiStore.add(entry);
        host.onEmojiSelected(entry, keepOpen);
    }
}
