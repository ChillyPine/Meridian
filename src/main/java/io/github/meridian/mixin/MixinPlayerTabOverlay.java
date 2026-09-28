package io.github.meridian.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.meridian.utils.NameGradients;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay {

    // Gradient only the name vanilla is about to draw — NOT getNameForDisplay's
    // return value in general. SkyHanni's compact tab list reads names through
    // getNameForDisplay and flattens them to strings, where any non-legacy RGB
    // color leaks out as literal "<#rrggbb>" tags. Hooking this one call site
    // leaves every other caller with the untouched name.
    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;")
    )
    private Component meridian$gradientTabName(Component name) {
        if (name == null) return null;
        return NameGradients.applyToTabName(name);
    }
}
