package io.github.meridian.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
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
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build()
    )

    val DEBUG_FILLED_BOX_NO_DEPTH: RenderPipeline = RenderPipelinesAccessor.`meridian$callRegister`(
        RenderPipeline.builder(RenderPipelinesAccessor.`meridian$getDebugFilledSnippet`())
            .withLocation(Identifier.fromNamespaceAndPath("meridian", "pipeline/debug_filled_box_no_depth"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
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
