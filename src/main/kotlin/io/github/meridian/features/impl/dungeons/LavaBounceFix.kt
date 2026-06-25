package io.github.meridian.features.impl.dungeons
//
//import io.github.meridian.Meridian
//import io.github.meridian.Meridian.mc
//import io.github.meridian.features.types.SwitchFeature
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//import net.minecraft.tags.FluidTags
//// This works on sim servers but not on Hypixel (tested on alpha).
//
//// TEMP / testing. In MC 26.x a Hypixel lava launch (~3.5 velY) clears the shallow
//// dungeon lava in the single launch tick, so vanilla's move-then-drag never gets to
//// apply the lava drag step to the upward velocity -> full launch is preserved =
//// superbounce to the ceiling. This re-applies the one missing vanilla drag step on
//// the launch tick to bring the bounce back to a normal height. Remove once verified.
//object LavaBounceFix : SwitchFeature(
//    name = "Lava Bounce Fix",
//    description = "Re-applies the lava drag MC 26 skips on a launch, so you don't superbounce to the ceiling.",
//    category = "Dungeons",
//    configKey = "lava_bounce_fix",
//    subcategory = "Misc",
//) {
//    private const val SHALLOW_Y_DRAG = 0.8
//    private const val DEEP_Y_DRAG = 0.62
//    private const val LAUNCH_THRESHOLD = 1.0
//
//    private var armed = false
//    private var lastLavaHeight = 0.0
//
//    init {
//        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
//            if (!enabled) {
//                armed = false
//                return@EndTick
//            }
//            val player = mc.player ?: return@EndTick
//
//            if (player.isInLava) {
//                armed = true
//                lastLavaHeight = player.getFluidHeight(FluidTags.LAVA)
//                return@EndTick
//            }
//
//            val vel = player.deltaMovement
//            if (armed && vel.y > LAUNCH_THRESHOLD) {
//                // Vanilla picks the drag by depth: shallow (<= jump threshold) damps Y to
//                // 0.8, deeper lava scales everything by 0.5. Match that off the last reading.
//                val drag = if (lastLavaHeight <= player.fluidJumpThreshold) SHALLOW_Y_DRAG else DEEP_Y_DRAG
//                player.deltaMovement = vel.multiply(1.0, drag, 1.0)
//                Meridian.logger.info(
//                    "[LavaBounceFix] launch damped: velY %.3f -> %.3f (drag %.2f, lavaH %.3f)".format(
//                        vel.y, vel.y * drag, drag, lastLavaHeight,
//                    )
//                )
//            }
//            armed = false
//        })
//    }
//}
