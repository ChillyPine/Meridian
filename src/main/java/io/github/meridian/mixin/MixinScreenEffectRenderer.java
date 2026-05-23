package io.github.meridian.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {
// RenderOptimizer.shouldDisableFireOverlay() is from odin and is their feature under the render optimizer category. Should replace with our own feature switch
//    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
//    private void meridian$onRenderFireOverlay(PoseStack poseStack, MultiBufferSource multiBufferSource, TextureAtlasSprite textureAtlasSprite, CallbackInfo ci) {
//        if (RenderOptimizer.shouldDisableFireOverlay()) {
//            ci.cancel();
//        }
//    }
}