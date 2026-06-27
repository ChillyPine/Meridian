package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.F5State
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.util.Optional

object LividHealthHUD : SwitchFeature(
    name = "Livid Health Display",
    description = "Shows Livid's remaining health as a HUD element during the F5/M5 boss.\nEdit the element position with /md hud.",
    category = "Dungeons",
    configKey = "livid_health_hud",
    subcategory = "M5",
){
    private val LIVID_BLOCK = BlockPos(5, 108, 42)

    private val blockToColor: Map<Block, String> = mapOf(
        Blocks.WHITE_STAINED_GLASS to "§f",   // Vendetta Livid
        Blocks.LIME_STAINED_GLASS to "§a",    // Smile Livid
        Blocks.GREEN_STAINED_GLASS to "§2",   // Frog Livid
        Blocks.RED_STAINED_GLASS to "§c",     // Hockey Livid
        Blocks.MAGENTA_STAINED_GLASS to "§d", // Crossed Livid
        Blocks.PURPLE_STAINED_GLASS to "§5",  // Purple Livid
        Blocks.GRAY_STAINED_GLASS to "§7",    // Doctor Livid
        Blocks.BLUE_STAINED_GLASS to "§9",    // Scream Livid
        Blocks.YELLOW_STAINED_GLASS to "§e",  // Arcade Livid
    )

    @Volatile private var lividHealth: String? = null

    private val element = object : HudElement(
        id = "livid_health_hud",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.5f,
    ) {
        override val shadow = false
        override fun content(): List<String> = lividHealth?.let { listOf(it) } ?: emptyList()
        override fun preview(): List<String> = listOf("§e﴾ §c§lLivid§r §a7M§c❤ §e﴿")
    }

    init {
        HudManager.register(element)

        onTick {
            if (!F5State.inF5Boss) {
                lividHealth = null
                return@onTick
            }
            update()
        }
    }

    override fun onDeactivate() {
        lividHealth = null
    }

    private fun update() {
        val level = mc.level ?: run { lividHealth = null; return }

        val colorCode = blockToColor[level.getBlockState(LIVID_BLOCK).block]
        if (colorCode == null) { lividHealth = null; return }

        for (ent in level.entitiesForRendering()) {
            if (ent !is ArmorStand) continue
            val name = ent.customName ?: continue
            val plain = name.string
            if (!plain.contains("Livid") || !plain.contains("❤")) continue
            // Match the color of the "Livid" text itself — the heart is always
            // red (§c), so substring-matching the whole nametag is ambiguous.
            if (name.colorOfText("Livid") != colorCode) continue
            lividHealth = name.toLegacy()
            return
        }
        lividHealth = null
    }
}

private fun Component.toLegacy(): String {
    val sb = StringBuilder()
    visit({ style, text ->
        sb.append(style.toLegacyCodes()).append(text)
        Optional.empty<Unit>()
    }, Style.EMPTY)
    return sb.toString()
}

private fun Style.toLegacyCodes(): String {
    val sb = StringBuilder()
    // Color must come first — a legacy color code resets all other formatting.
    colorCode()?.let { sb.append(it) }
    if (isBold) sb.append("§l")
    if (isItalic) sb.append("§o")
    if (isUnderlined) sb.append("§n")
    if (isStrikethrough) sb.append("§m")
    if (isObfuscated) sb.append("§k")
    return sb.toString()
}

// The §-code for this style's color, or null if it has none / isn't a named color.
private fun Style.colorCode(): String? {
    val rgb = color?.value ?: return null
    val fmt = ChatFormatting.entries.firstOrNull { it.isColor && it.color == rgb } ?: return null
    return "§${fmt.char}"
}

// The §-color of the first text segment containing [needle], or null if absent.
private fun Component.colorOfText(needle: String): String? {
    var result: String? = null
    visit({ style, text ->
        if (text.contains(needle)) {
            result = style.colorCode()
            Optional.of(Unit) // stop visiting
        } else {
            Optional.empty<Unit>()
        }
    }, Style.EMPTY)
    return result
}
