package io.github.meridian.mixin;

import io.github.meridian.features.impl.general.PlayerNametag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {

    // Vanilla returns false here for the camera entity, which is why your own
    // name vanishes in third person. When the feature is on, force it true for
    // the local player outside first-person so the nametag renders above your
    // head. (Player.shouldShowName() is already true, so AvatarRenderer's
    // override passes this result straight through.) Targets the generic
    // overload by descriptor to avoid matching the Entity bridge method.
    @Inject(
        method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void meridian$showOwnNameInThirdPerson(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (!PlayerNametag.INSTANCE.getEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player && !mc.options.getCameraType().isFirstPerson()) {
            cir.setReturnValue(true);
        }
    }
}