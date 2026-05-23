package io.github.meridian.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer/*<T extends Entity>*/ {
// Hiding players, hiding dying entity armor stands, death animation
//    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
//    private void onRender(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
//
//    }
}