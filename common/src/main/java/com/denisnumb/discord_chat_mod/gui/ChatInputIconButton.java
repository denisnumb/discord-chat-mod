package com.denisnumb.discord_chat_mod.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ChatInputIconButton extends AbstractWidget {
    private static final int HOVER_COLOR = 0x30FFFFFF;
    private static final int PRESSED_COLOR = 0x50FFFFFF;

    private final Consumer<ChatInputIconButton> onPress;
    private final BooleanSupplier pressed;

    public ChatInputIconButton(int x, int y, int size, Component icon, Component tooltip,
                               Consumer<ChatInputIconButton> onPress) {
        this(x, y, size, icon, tooltip, onPress, () -> false);
    }

    public ChatInputIconButton(int x, int y, int size, Component icon, Component tooltip,
                               Consumer<ChatInputIconButton> onPress, BooleanSupplier pressed) {
        super(x, y, size, size, icon);
        this.onPress = onPress;
        this.pressed = pressed;
        this.setFocused(false);
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (pressed.getAsBoolean())
            graphics.fill(getX(), getY(), getX() + width, getY() + height, PRESSED_COLOR);
        else if (this.isHovered())
            graphics.fill(getX(), getY(), getX() + width, getY() + height, HOVER_COLOR);

        if (this.isHovered())
            graphics.requestCursor(CursorTypes.POINTING_HAND);

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 8) / 2,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent mouseButtonEvent, boolean bl) {
        onPress.accept(this);
    }

    @Override
    public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
