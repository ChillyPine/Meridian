package io.github.meridian.mixin;

import io.github.meridian.features.impl.vanilla.RemoveRealms;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
    @Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
    private void meridian$hideRealmsNotifications(CallbackInfoReturnable<Boolean> cir) {
        if (RemoveRealms.INSTANCE.getEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
