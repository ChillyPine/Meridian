package io.github.meridian.mixin;

import io.github.meridian.features.impl.general.RemoveChatBar;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Suppresses the GuiMessageTag indicator bar drawn while the chat screen is
// open. Cancelling at HEAD also skips the showTooltip(...) call that follows
// the fill, so the "Server message." hover text is gone too. Scoped to the
// system tags via logTag "System" and leaves the tag intact for logging — see
// MixinChatTagBackground for the rationale.
@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
public class MixinChatTagFocused {

    @Inject(method = "handleTag(IIIIFLnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void meridian$hideSystemTag(int x0, int y0, int x1, int y1, float alpha, GuiMessageTag tag, CallbackInfo ci) {
        if (RemoveChatBar.INSTANCE.getEnabled() && tag != null && "System".equals(tag.logTag())) {
            ci.cancel();
        }
    }
}