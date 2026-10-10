package io.github.meridian.utils

import com.mojang.renderpearl.api.pipeline.ColorTargetState
import com.mojang.renderpearl.api.pipeline.CompareOp
import com.mojang.renderpearl.api.pipeline.DepthStencilState
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import io.github.meridian.mixin.accessor.RenderPipelinesAccessor
import io.github.meridian.mixin.accessor.RenderTypeInvoker
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier

// No-depth-test variants of vanilla LINES and DEBUG_FILLED_BOX. The vanilla snippets carry
// shaders/format/uniforms; we override depth test + depth write and route through
// RenderPipelines.register so the GL backend precompiles them with the rest.
object CustomRenderPipelines {

    val LINES_NO_DEPTH: RenderPipeline = RenderPipelinesAccessor.`meridian$callRegister`(
        RenderPipeline.builder(RenderPipelinesAccessor.`meridian$getLinesSnippet`())
            .withLocation(Identifier.fromNamespaceAndPath("meridian", "pipeline/lines_no_depth"))
            // Since 26.3 LINES_SNIPPET carries no color target (its OIT variant needs
            // different ones); without this the pipeline declares 0 targets and the
            // pass's color-attachment check crashes the frame. Vanilla LINES does the same.
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build()
    )

    val DEBUG_FILLED_BOX_NO_DEPTH: RenderPipeline = RenderPipelinesAccessor.`meridian$callRegister`(
        RenderPipeline.builder(RenderPipelinesAccessor.`meridian$getDebugFilledSnippet`())
            .withLocation(Identifier.fromNamespaceAndPath("meridian", "pipeline/debug_filled_box_no_depth"))
            .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build()
    )

    val LINES_NO_DEPTH_TYPE: RenderType = RenderTypeInvoker.`meridian$callCreate`(
        "meridian_lines_no_depth",
        RenderSetup.builder(LINES_NO_DEPTH).createRenderSetup()
    )

    val DEBUG_FILLED_BOX_NO_DEPTH_TYPE: RenderType = RenderTypeInvoker.`meridian$callCreate`(
        "meridian_debug_filled_box_no_depth",
        RenderSetup.builder(DEBUG_FILLED_BOX_NO_DEPTH).createRenderSetup()
    )
}
