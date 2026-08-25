package io.github.meridian.mixin;

import io.github.meridian.utils.NameGradients;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerTabOverlay.class, priority = 2000) // lower prio than skyhanni since they think they are important or something
public abstract class MixinPlayerTabOverlay {

    // getNameForDisplay builds the Component shown for each entry in the
    // tab/player list. Gradient any tracked IGN inside it (preserving the rest
    // of the styling); returns the same instance when nothing matches.
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void meridian$gradientTabName(CallbackInfoReturnable<Component> cir) {
        Component name = cir.getReturnValue();
        if (name == null) return;
        Component gradient = NameGradients.applyToTabName(name);
        if (gradient != name) cir.setReturnValue(gradient);
    }
}