package io.github.meridian.utils

import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.meridian.Meridian
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/**
 * Central ESP utility. Per-feature ESP code calls into here from a
 * WorldRenderEvents.AFTER_ENTITIES handler.
 *
 * Two styles:
 *   BOX        - wireframe AABB
 *   FILLED_BOX - translucent filled AABB plus its wireframe edge
 *
 * All draws are anchored to the entity's interpolated render position so
 * the box doesn't lag behind moving entities (entities tick at 20Hz, the
 * world renders at framerate).
 *
 * Depth:
 *   ESP.depth, toggled by `/md depth`. When true (default) vanilla LINES /
 *   debugFilledBox render types are used (LEQUAL depth → LOS-gated). When
 *   false, the custom no-depth pipelines in [CustomRenderPipelines] are used,
 *   so boxes and tracers render through walls.
 */
object ESP {

    enum class Style { BOX, FILLED_BOX }

    @Volatile var depth: Boolean = true

    private fun partialTick(): Float =
        Meridian.mc.deltaTracker.getGameTimeDeltaPartialTick(true)

    // ---- Entity-anchored draws -------------------------------------------

    private fun renderBoundingBox(entity: Entity): net.minecraft.world.phys.AABB {
        val p = entity.getPosition(partialTick())
        return entity.boundingBox.move(p.x - entity.x, p.y - entity.y, p.z - entity.z)
    }

    /** Wireframe box around the entity's interpolated bounding box. */
    fun drawBox(ctx: WorldRenderContext, entity: Entity, argb: Int, depth: Boolean = ESP.depth) {
        val bb = renderBoundingBox(entity)
        drawBoxAt(ctx, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, argb, depth)
    }

    /** Filled translucent box plus a wireframe edge. */
    fun drawFilled(ctx: WorldRenderContext, entity: Entity, argb: Int, depth: Boolean = ESP.depth) {
        val bb = renderBoundingBox(entity)
        drawFilledAt(ctx, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, halfAlpha(argb), depth)
        drawBoxAt(ctx, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, argb, depth)
    }

    /**
     * Fixed-size wireframe box anchored to the entity's interpolated
     * position. Useful for armor-stand-anchored ESPs where the visible
     * mob isn't the armor stand itself.
     *
     * x/y/z is the box center-bottom; w/h/wz are full extents on each
     * axis; yOffset shifts the bottom up/down from the entity's feet.
     */
    fun drawBox(
        ctx: WorldRenderContext,
        entity: Entity,
        w: Double, h: Double, wz: Double,
        yOffset: Double = 0.0,
        argb: Int,
        depth: Boolean = ESP.depth,
    ) {
        val p = entity.getPosition(partialTick())
        val cy = p.y + yOffset
        val hx = w / 2.0
        val hz = wz / 2.0
        drawBoxAt(ctx, p.x - hx, cy, p.z - hz, p.x + hx, cy + h, p.z + hz, argb, depth)
    }

    /** Fixed-size filled box variant of the above. */
    fun drawFilled(
        ctx: WorldRenderContext,
        entity: Entity,
        w: Double, h: Double, wz: Double,
        yOffset: Double = 0.0,
        argb: Int,
        depth: Boolean = ESP.depth,
    ) {
        val p = entity.getPosition(partialTick())
        val cy = p.y + yOffset
        val hx = w / 2.0
        val hz = wz / 2.0
        drawFilledAt(ctx, p.x - hx, cy, p.z - hz, p.x + hx, cy + h, p.z + hz, halfAlpha(argb), depth)
        drawBoxAt(ctx, p.x - hx, cy, p.z - hz, p.x + hx, cy + h, p.z + hz, argb, depth)
    }

    /**
     * Eye-to-target tracer.
     *
     * GPU depth-testing doesn't work reliably for MC's line geometry (lines are
     * expanded to screen-space quads, fragments near the camera always pass the
     * depth test). So we always render lines through the no-depth pipeline and
     * gate visibility ourselves with a CPU raycast: when depth=true and the
     * target is occluded by a block, the tracer is skipped.
     */
    fun drawTracer(
        ctx: WorldRenderContext,
        x: Double, y: Double, z: Double,
        argb: Int,
        depth: Boolean = ESP.depth,
    ) {
        if (depth && !hasLineOfSight(x, y, z)) return
        val pose = ctx.matrices()
        val consumers = ctx.consumers() as? MultiBufferSource.BufferSource ?: return
        val cam = Meridian.mc.gameRenderer.mainCamera
        val camPos = cam.position()
        val look = cam.forwardVector()
        val sx = camPos.x + look.x() * 0.2
        val sy = camPos.y + look.y() * 0.2
        val sz = camPos.z + look.z() * 0.2
        pose.pushPose()
        pose.translate(-camPos.x, -camPos.y, -camPos.z)
        val rt = CustomRenderPipelines.LINES_NO_DEPTH_TYPE
        val buf = consumers.getBuffer(rt)
        val last = pose.last()
        val m = last.pose()
        val dx = (x - sx).toFloat()
        val dy = (y - sy).toFloat()
        val dz = (z - sz).toFloat()
        val len = kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble())
            .toFloat().coerceAtLeast(1e-4f)
        val nx = dx / len
        val ny = dy / len
        val nz = dz / len
        buf.addVertex(m, sx.toFloat(), sy.toFloat(), sz.toFloat())
            .setColor(argb).setNormal(last, nx, ny, nz).setLineWidth(2f)
        buf.addVertex(m, x.toFloat(), y.toFloat(), z.toFloat())
            .setColor(argb).setNormal(last, nx, ny, nz).setLineWidth(2f)
        pose.popPose()
        consumers.endBatch(rt)
    }

    // ---- Low-level primitives --------------------------------------------

    private fun linesType(depth: Boolean) =
        if (depth) RenderTypes.lines() else CustomRenderPipelines.LINES_NO_DEPTH_TYPE

    private fun filledBoxType(depth: Boolean) =
        if (depth) RenderTypes.debugFilledBox() else CustomRenderPipelines.DEBUG_FILLED_BOX_NO_DEPTH_TYPE

    private fun hasLineOfSight(x: Double, y: Double, z: Double): Boolean {
        val mc = Meridian.mc
        val level = mc.level ?: return true
        val player = mc.player ?: return true
        val camPos = mc.gameRenderer.mainCamera.position()
        val ctx = ClipContext(
            Vec3(camPos.x, camPos.y, camPos.z),
            Vec3(x, y, z),
            ClipContext.Block.VISUAL,
            ClipContext.Fluid.NONE,
            player,
        )
        return level.clip(ctx).type == HitResult.Type.MISS
    }

    private fun drawBoxAt(
        ctx: WorldRenderContext,
        x0: Double, y0: Double, z0: Double,
        x1: Double, y1: Double, z1: Double,
        argb: Int,
        depth: Boolean,
    ) {
        val pose = ctx.matrices()
        val consumers = ctx.consumers() as? MultiBufferSource.BufferSource ?: return
        val cam = Meridian.mc.gameRenderer.mainCamera.position()
        pose.pushPose()
        pose.translate(-cam.x, -cam.y, -cam.z)
        val rt = linesType(depth)
        val buf = consumers.getBuffer(rt)
        drawWireBox(buf, pose.last(), x0.toFloat(), y0.toFloat(), z0.toFloat(), x1.toFloat(), y1.toFloat(), z1.toFloat(), argb)
        pose.popPose()
        consumers.endBatch(rt)
    }

    private fun drawFilledAt(
        ctx: WorldRenderContext,
        x0: Double, y0: Double, z0: Double,
        x1: Double, y1: Double, z1: Double,
        argb: Int,
        depth: Boolean,
    ) {
        val pose = ctx.matrices()
        val consumers = ctx.consumers() as? MultiBufferSource.BufferSource ?: return
        val cam = Meridian.mc.gameRenderer.mainCamera.position()
        pose.pushPose()
        pose.translate(-cam.x, -cam.y, -cam.z)
        val rt = filledBoxType(depth)
        val buf = consumers.getBuffer(rt)
        val m = pose.last().pose()
        val fx0 = x0.toFloat(); val fy0 = y0.toFloat(); val fz0 = z0.toFloat()
        val fx1 = x1.toFloat(); val fy1 = y1.toFloat(); val fz1 = z1.toFloat()
        fun v(px: Float, py: Float, pz: Float) { buf.addVertex(m, px, py, pz).setColor(argb) }
        v(fx0, fy0, fz0); v(fx1, fy0, fz0); v(fx1, fy0, fz1); v(fx0, fy0, fz1)        // -Y
        v(fx0, fy1, fz0); v(fx0, fy1, fz1); v(fx1, fy1, fz1); v(fx1, fy1, fz0)        // +Y
        v(fx0, fy0, fz0); v(fx0, fy1, fz0); v(fx1, fy1, fz0); v(fx1, fy0, fz0)        // -Z
        v(fx0, fy0, fz1); v(fx1, fy0, fz1); v(fx1, fy1, fz1); v(fx0, fy1, fz1)        // +Z
        v(fx0, fy0, fz0); v(fx0, fy0, fz1); v(fx0, fy1, fz1); v(fx0, fy1, fz0)        // -X
        v(fx1, fy0, fz0); v(fx1, fy1, fz0); v(fx1, fy1, fz1); v(fx1, fy0, fz1)        // +X
        pose.popPose()
        consumers.endBatch(rt)
    }

    private fun drawWireBox(
        buf: VertexConsumer,
        last: com.mojang.blaze3d.vertex.PoseStack.Pose,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        argb: Int,
    ) {
        val m = last.pose()
        val lw = 2f
        fun line(ax: Float, ay: Float, az: Float, bx: Float, by: Float, bz: Float, nx: Float, ny: Float, nz: Float) {
            buf.addVertex(m, ax, ay, az).setColor(argb).setNormal(last, nx, ny, nz).setLineWidth(lw)
            buf.addVertex(m, bx, by, bz).setColor(argb).setNormal(last, nx, ny, nz).setLineWidth(lw)
        }
        line(x0, y0, z0, x1, y0, z0, 1f, 0f, 0f)
        line(x1, y0, z0, x1, y0, z1, 0f, 0f, 1f)
        line(x1, y0, z1, x0, y0, z1, -1f, 0f, 0f)
        line(x0, y0, z1, x0, y0, z0, 0f, 0f, -1f)
        line(x0, y1, z0, x1, y1, z0, 1f, 0f, 0f)
        line(x1, y1, z0, x1, y1, z1, 0f, 0f, 1f)
        line(x1, y1, z1, x0, y1, z1, -1f, 0f, 0f)
        line(x0, y1, z1, x0, y1, z0, 0f, 0f, -1f)
        line(x0, y0, z0, x0, y1, z0, 0f, 1f, 0f)
        line(x1, y0, z0, x1, y1, z0, 0f, 1f, 0f)
        line(x1, y0, z1, x1, y1, z1, 0f, 1f, 0f)
        line(x0, y0, z1, x0, y1, z1, 0f, 1f, 0f)
    }

    private fun halfAlpha(argb: Int): Int {
        val a = ((argb ushr 24) and 0xFF) / 2
        return (a shl 24) or (argb and 0x00FFFFFF)
    }

    // ---- Persistence ------------------------------------------------------

    fun loadFrom(json: com.google.gson.JsonObject) {
        if (json.has("depth")) depth = json.get("depth").asBoolean
    }

    fun saveTo(json: com.google.gson.JsonObject) {
        json.addProperty("depth", depth)
    }
}
