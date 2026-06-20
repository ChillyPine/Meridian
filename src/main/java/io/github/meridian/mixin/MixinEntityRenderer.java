package io.github.meridian.mixin;

import io.github.meridian.utils.NameGradients;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    // The name shown above an entity's head flows through getNameTag (it feeds
    // EntityRenderState.nameTag). Players inherit this base method — neither
    // LivingEntityRenderer nor AvatarRenderer overrides it — so rewriting the
    // return value here gradients tracked IGNs above their heads. Returns the
    // same instance when no IGN matches, so the cancel only fires when needed.
    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void meridian$gradientNameTag(CallbackInfoReturnable<Component> cir) {
        Component name = cir.getReturnValue();
        if (name == null) return;
        Component gradient = NameGradients.applyToNameTag(name);
        if (gradient != name) cir.setReturnValue(gradient);
    }
}