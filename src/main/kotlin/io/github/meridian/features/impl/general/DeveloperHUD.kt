package io.github.meridian.features.impl.general

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.BossPhase
import io.github.meridian.utils.BossState
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.F4State
import io.github.meridian.utils.F5State
import io.github.meridian.utils.F6State
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.SkyblockLocation
import io.github.meridian.utils.Keybinds.boundKeyLabel
import io.github.meridian.utils.isDeveloper
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.Locale

object DeveloperHUD : SwitchFeature(
    name = "Developer HUD",
    description = "Displays info you prob don't need unless ur making code for this mod",
    category = "General",
    configKey = "developer_hud",
    subcategory = "Developer",
) {
    private const val TITLE = "§lDeveloper HUD"

    private var showDecimals = false

    init {
        onTick {
            // drain fully so multiple presses in one tick each toggle
            val key = Keybinds.devHudDecimals ?: return@onTick
            while (key.consumeClick()) showDecimals = !showDecimals
        }
    }

    private fun maxLookRange(): Double = (mc.options.effectiveRenderDistance + 1) * 16.0

    private fun keyLabel(): String = "§7[${Keybinds.devHudDecimals?.boundKeyLabel() ?: "?"}]"

    private fun lines(): List<String> {
        val player = mc.player ?: return emptyList()
        val level = mc.level ?: return emptyList()

        val pos = player.blockPosition()
        val xyz = if (showDecimals) {
            String.format(Locale.ROOT, "%.3f, %.3f, %.3f", player.x, player.y, player.z)
        } else "${pos.x}, ${pos.y}, ${pos.z}"

        val hit = player.pick(maxLookRange(), 0f, false)
        var target = "None"
        var targetBlock = "None"
        if (hit.type == HitResult.Type.BLOCK) {
            val b = (hit as BlockHitResult).blockPos
            val block = level.getBlockState(b).block
            val dist = player.getEyePosition(0f).distanceTo(hit.location)
            target = "${b.x}, ${b.y}, ${b.z} §7[${String.format(Locale.ROOT, "%.1f", dist)} blocks]"
            targetBlock = "${block.name.string} §8(${BuiltInRegistries.BLOCK.getKey(block)})"
        }

        val biome = level.getBiome(pos).registeredName

        return listOf(
            TITLE,
            "§7XYZ: §f$xyz ${keyLabel()}",
            "§7Looking At: §f$target",
            "§7Target Block: §f$targetBlock",
            "§7Biome: §f$biome",
            "",
        ) + locationLines()
    }

    // What the mod itself thinks about where we are (SkyblockLocation / PlayerLocation),
    private fun locationLines(): List<String> {
        val sb = SkyblockLocation
        val island = sb.island
        val out = mutableListOf(
            "§7Hypixel: ${yesNo(sb.onHypixel)} §7Skyblock: ${yesNo(sb.onSkyblock)}",
            "§7Island: §f${island.displayName} §8(${island.id})",
            "§7Area: §f${sb.area.ifEmpty { "None" }}",
            "§7Server: §f${sb.server.ifEmpty { "None" }}",
            "§7In Dungeon: ${yesNo(DungeonState.inDungeon)}",
        )
        if (DungeonState.inDungeon) out += "§7Boss: §f${bossLabel()}"
        return out
    }

    private fun bossLabel(): String {
        BossPhase.entries.firstOrNull { BossState[it] }?.let { return "M7/F7 ${it.label}" }
        return when {
            F4State.inF4Boss -> "F4/M4"
            F5State.inF5Boss -> "F5/M5"
            F6State.inF6Boss -> "F6/M6"
            else -> "None"
        }
    }

    private fun yesNo(b: Boolean): String = if (b) "§fYes" else "§fNo"

    private val element = object : HudElement(
        id = "developer_hud",
        name = name,
        defaultAnchorX = 0.01f,
        defaultAnchorY = 0.3f,
    ) {
        override fun content(): List<String> = if (isActive()) lines() else emptyList()
        override fun preview(): List<String> = lines().ifEmpty {
            listOf(
                TITLE,
                "§7XYZ: §f0, 64, 0 ${keyLabel()}",
                "§7Looking At: §f0, 63, 1 §7[2.4 blocks]",
                "§7Target Block: §fGrass Block §8(minecraft:grass_block)",
                "§7Biome: §fminecraft:plains",
                "",
                "§7Hypixel: §fYes §7Skyblock: §fYes",
                "§7Island: §fHub §8(hub)",
                "§7Area: §fVillage",
                "§7Server: §fmini1A",
                "§7In Dungeon: §fNo",
            )
        }
    }

    init {
        if (isDeveloper) {
            HudManager.register(element, this)
        }
    }
}
