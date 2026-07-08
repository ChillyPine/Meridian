package io.github.meridian.mixin;

import io.github.meridian.features.impl.vanilla.ChatEraser;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Backspace normally deletes a character in the chat input box. When the cursor
// is hovering over a rendered chat line, ChatEraser instead consumes the key and
// drops that message from the display — so text-editing is only overridden while
// pointing at a message, which is the feature's gate. GLFW_KEY_BACKSPACE = 259.
@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void meridian$eraseHoveredMessage(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() == 259 && ChatEraser.INSTANCE.getEnabled() && ChatEraser.INSTANCE.eraseHoveredMessage()) {
            cir.setReturnValue(true);
        }
    }
}
