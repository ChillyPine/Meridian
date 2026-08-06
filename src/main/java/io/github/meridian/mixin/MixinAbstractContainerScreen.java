package io.github.meridian.mixin;

import io.github.meridian.gui.InventorySearch;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hosts the always-on inventory search bar. Char input is not routed here —
// AbstractContainerScreen doesn't override charTyped, so that hook lives in
// MixinKeyboardHandler.
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow @Final protected AbstractContainerMenu menu;

    // The creative screen brings its own search field and tabbed slot layout.
    private boolean meridian$searchable() {
        return !((Object) this instanceof CreativeModeInventoryScreen);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void meridian$resetSearchFocus(CallbackInfo ci) {
        if (meridian$searchable()) {
            InventorySearch.INSTANCE.onScreenOpen();
        }
    }

    // HEAD of extractTooltip is after the slots and their items have been
    // extracted but before the hover tooltip, so the dim overlay covers items
    // without covering the tooltip.
    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void meridian$renderSearch(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        if (meridian$searchable()) {
            InventorySearch.INSTANCE.render(g, this.menu.slots, this.leftPos, this.topPos);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void meridian$searchMouseClicked(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (meridian$searchable() && InventorySearch.INSTANCE.mouseClicked(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void meridian$searchKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (meridian$searchable() && InventorySearch.INSTANCE.keyPressed(event)) {
            cir.setReturnValue(true);
        }
    }
}
