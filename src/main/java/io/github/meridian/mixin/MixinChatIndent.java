package io.github.meridian.mixin;

import io.github.meridian.features.impl.general.RemoveChatBar;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Removes the 4px (MESSAGE_INDENT) left gutter that vanilla reserves for the
// chat tag bar. The indent is a single global pose translate(4.0f, 0) applied
// to the whole chat via updatePose, built in the synthetic lambda
// lambda$extractRenderState$0 — so zeroing the x arg shifts every chat line
// flush-left. Gated on the same toggle that hides the bar, so the reclaimed
// space only disappears when the bar is also gone.
//
// NOTE: this targets a compiler-generated lambda name. If a future MC reorders
// lambdas it fails loudly at load (defaultRequire = 1), not silently.
@Mixin(ChatComponent.class)
public class MixinChatIndent {

    @ModifyArg(
        method = "lambda$extractRenderState$0(FLorg/joml/Matrix3x2f;)V",
        at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2f;translate(FF)Lorg/joml/Matrix3x2f;"),
        index = 0
    )
    private static float meridian$removeChatIndent(float x) {
        return RemoveChatBar.INSTANCE.getEnabled() ? 0.0f : x;
    }
}