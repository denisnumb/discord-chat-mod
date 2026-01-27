package com.denisnumb.discord_chat_mod.neoforge.mixin;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @Redirect(
            method = "lambda$grab$1",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"
            )
    )
    private static void redirectScreenshotMessage(Consumer<Component> consumer, Object componentObj) {
        if (componentObj instanceof Component component){
            List<Component> flat = component.toFlatList();

            if (flat.size() > 1 && flat.get(1).getStyle().getClickEvent() instanceof ClickEvent.OpenFile openFileEvent)
                consumer.accept(MinecraftClientEvents.handleScreenshot(openFileEvent.file()));
            else
                consumer.accept(component);
        }
    }
}
