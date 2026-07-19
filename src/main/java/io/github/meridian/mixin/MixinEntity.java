package io.github.meridian.mixin;

import io.github.meridian.features.impl.carryhelper.HighlightClients;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    // Client-side setGlowingTag is a no-op for forcing glow (isCurrentlyGlowing
    // reads the synced flag, not the tag), so Highlight Client(s) drives the
    // glow here: force isCurrentlyGlowing true for tracked carry clients. The
    // outline color is set separately in MixinEntityRenderer.
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void meridian$carryGlow(CallbackInfoReturnable<Boolean> cir) {
        if (HighlightClients.INSTANCE.shouldGlow((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
