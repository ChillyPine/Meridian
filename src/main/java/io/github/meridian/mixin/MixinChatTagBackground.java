package io.github.meridian.mixin;

import io.github.meridian.features.impl.vanilla.RemoveChatBar;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Suppresses the GuiMessageTag indicator bar drawn in the unfocused chat HUD
// overlay. Scoped to the system tags via logTag "System" — this matches both
// system() and systemSinglePlayer() (26.1.2 routes server/client system
// messages through systemSinglePlayer, whose tooltip reads "Server message.")
// while leaving chatModified / chatNotSecure / chatError warnings to render.
// Matching on logTag is resilient to upstream tooltip-text changes. The tag
// object itself is untouched, so ChatComponent.logChatMessage still writes
// "[System]".
@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess")
public class MixinChatTagBackground {

    @Inject(method = "handleTag(IIIIFLnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void meridian$hideSystemTag(int x0, int y0, int x1, int y1, float alpha, GuiMessageTag tag, CallbackInfo ci) {
        if (RemoveChatBar.INSTANCE.getEnabled() && tag != null && "System".equals(tag.logTag())) {
            ci.cancel();
        }
    }
}