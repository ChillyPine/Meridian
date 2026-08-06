package io.github.meridian.mixin;

import io.github.meridian.gui.InventorySearch;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// AbstractContainerScreen inherits charTyped as an interface default rather
// than overriding it, so there is no screen-side method to inject into. Feed
// the inventory search bar from the keyboard dispatch instead.
@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Inject(method = "charTyped(JLnet/minecraft/client/input/CharacterEvent;)V", at = @At("HEAD"), cancellable = true)
    private void meridian$searchCharTyped(long window, CharacterEvent event, CallbackInfo ci) {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen<?> && !(screen instanceof CreativeModeInventoryScreen)
                && InventorySearch.INSTANCE.charTyped(event)) {
            ci.cancel();
        }
    }
}
