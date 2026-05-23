package io.github.meridian.mixin;

import io.github.meridian.gui.MeridianScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
}