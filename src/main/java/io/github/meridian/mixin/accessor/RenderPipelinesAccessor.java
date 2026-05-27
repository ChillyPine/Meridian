package io.github.meridian.mixin.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("LINES_SNIPPET")
    static RenderPipeline.Snippet meridian$getLinesSnippet() { throw new AssertionError(); }

    @Accessor("DEBUG_FILLED_SNIPPET")
    static RenderPipeline.Snippet meridian$getDebugFilledSnippet() { throw new AssertionError(); }

    @Invoker("register")
    static RenderPipeline meridian$callRegister(RenderPipeline pipeline) { throw new AssertionError(); }
}