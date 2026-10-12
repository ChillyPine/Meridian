package io.github.meridian.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.meridian.utils.NameGradients;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// SkyHanni's compact tab list doesn't draw getNameForDisplay's result — it parses
// it to a string, then rebuilds each player line from §-coded strings in
// createCustomName(). Gradient that rebuilt Component: parsing is already done,
// so RGB here can't leak into SkyHanni's string-based name/sort handling.
// @Pseudo + require = 0: no-op when SkyHanni is absent or renames the method.
@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.features.misc.compacttablist.AdvancedPlayerList")
public abstract class MixinSkyHanniAdvancedPlayerList {

    @ModifyReturnValue(method = "createCustomName", at = @At("RETURN"), require = 0)
    private Component meridian$gradientCompactTabName(Component name) {
        if (name == null) return null;
        return NameGradients.applyToTabName(name);
    }
}