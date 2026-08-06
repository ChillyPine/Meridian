package io.github.meridian.utils

import io.github.meridian.Meridian.mc
import net.minecraft.ChatFormatting
import net.minecraft.world.scores.DisplaySlot

fun sidebarLines(): List<String> {
    val level = mc.level ?: return emptyList()
    val scoreboard = level.scoreboard
    val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()

    val lines = ArrayList<String>()
    for (holder in scoreboard.trackedPlayers) {
        if (!scoreboard.listPlayerScores(holder).containsKey(objective)) continue
        val team = scoreboard.getPlayersTeam(holder.scoreboardName) ?: continue
        val raw = team.playerPrefix.string + team.playerSuffix.string
        if (raw.isBlank()) continue
        lines += (ChatFormatting.stripFormatting(raw) ?: raw)
    }
    lines.reverse()
    return lines
}

/** The sidebar header — on Skyblock this is the `SKYBLOCK` / `SKYBLOCK CO-OP` banner. */
fun sidebarTitle(): String {
    val level = mc.level ?: return ""
    val objective = level.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return ""
    val raw = objective.displayName.string
    return ChatFormatting.stripFormatting(raw) ?: raw
}

fun sidebarLineContaining(text: String): String? =
    sidebarLines().firstOrNull { it.contains(text) }
