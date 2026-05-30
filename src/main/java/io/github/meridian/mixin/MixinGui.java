package io.github.meridian.mixin;

import io.github.meridian.features.impl.general.RemoveNausea;
import io.github.meridian.gui.MeridianScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void meridian$hideCrosshairOnMeridianScreen(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof MeridianScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "renderConfusionOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void noMoreNausea(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (RemoveNausea.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}