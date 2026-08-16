package io.github.meridian.utils

import com.mojang.blaze3d.platform.InputConstants
import io.github.meridian.Meridian
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

/**
 * Meridian's entries in the vanilla Controls screen. Names here are translation
 * keys — every mapping needs a matching line in `assets/meridian/lang/en_us.json`,
 * and the category label key is `key.category.meridian.<path>`.
 *
 * Poll a mapping with `consumeClick()` from a tick handler. Drain it every tick
 * even when the feature can't act on it, otherwise a press made outside the
 * feature's window fires the moment the window opens.
 */
object Keybinds {
    private val CATEGORY: KeyMapping.Category =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Meridian.MOD_ID, "main"))

    val beeheemothWarp = KeyMapping(
        "key.meridian.beeheemoth_warp",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        CATEGORY,
    )

    private val all = listOf(beeheemothWarp)

    fun init() {
        all.forEach { KeyMappingHelper.registerKeyMapping(it) }
    }

    /** The bound key as shown in the Controls screen, e.g. "H" — for use in titles/HUDs. */
    fun KeyMapping.boundKeyLabel(): String = translatedKeyMessage.string
}
