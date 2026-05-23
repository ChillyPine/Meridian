package io.github.meridian.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
// Message sent even is an odin import defined in their code
//    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
//    private void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
//        if (new MessageSentEvent(message).postAndCatch()) ci.cancel();
//    }
}
