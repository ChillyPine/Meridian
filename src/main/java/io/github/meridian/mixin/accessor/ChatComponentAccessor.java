package io.github.meridian.mixin.accessor;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

// Exposes the private layout state ChatEraser needs to map a screen-space mouse
// position to the GuiMessage under it, and to rebuild the display after dropping
// a message. Removal is display-only: allMessages/trimmedMessages are the render
// buffers, so logChatMessage has already written the log by the time we touch them.
@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
    @Accessor("allMessages")
    List<GuiMessage> meridian$getAllMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> meridian$getTrimmedMessages();

    @Accessor("chatScrollbarPos")
    int meridian$getChatScrollbarPos();

    @Accessor("chatScrollbarPos")
    void meridian$setChatScrollbarPos(int pos);

    @Invoker("getScale")
    double meridian$getScale();

    @Invoker("getWidth")
    int meridian$getWidth();

    @Invoker("refreshTrimmedMessages")
    void meridian$refreshTrimmedMessages();
}
