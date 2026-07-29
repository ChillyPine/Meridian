package io.github.meridian.features.impl.dungeons

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import io.github.meridian.utils.DungeonState

object QuizFeatures : SwitchFeature(
    name = "Quiz Progress Hud",
    description = "Displays Quiz ( x / 3 ) as a hud element",
    category = "Dungeons",
    configKey = "quiz_hud",
    subcategory = "Clear",
) {

    private var progress: Int = -1 // -1 = not started, 0-3 = questions answered
    private var completedAt: Long? = null

    private val startRegex = Regex("^\\[STATUE] Oruo the Omniscient: I am Oruo the Omniscient\\. I have lived many lives\\. I have learned all there is to know\\.$")
    private val q1Regex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered Question #1 correctly!$")
    private val q2Regex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered Question #2 correctly!$")
    private val finalRegex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered the final question correctly!$")

    private val element = object : HudElement(
        id = "quiz_progress",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.5f,
    ) {
        override val shadow = true

        override fun content(): List<String> {
            val completed = completedAt
            if (completed != null) {
                if (System.currentTimeMillis() - completed >= 2000L) {
                    progress = -1
                    completedAt = null
                    return emptyList()
                }
                return listOf("§a§lQuiz Complete!")
            }

            if (progress < 0) return emptyList()

            val numberColor = when (progress) {
                0, 1 -> "§c" 
                2 -> "§e"
                else -> "§8" // fallback if this hits I fucked sum
            }

            return listOf("§6§l✦§5§l Quiz ($numberColor§l$progress§5§l/§a§l3§5§l)§6§l ✦") // whats being rendered live
        }

        override fun preview(): List<String> = listOf("§6§l✦§5§l Quiz (§c§l0§5§l/§a§l3§5§l)§6§l ✦") // preview in hud
    }

    init {
        HudManager.register(element)
        onChat(DungeonState.state) { text, _, _ ->
            when {
                startRegex.matches(text) -> {
                    progress = 0
                    completedAt = null
                }
                q1Regex.matches(text) -> progress = 1
                q2Regex.matches(text) -> progress = 2
                finalRegex.matches(text) -> {
                    progress = 3
                    completedAt = System.currentTimeMillis()
                }
            }
        }
    }
}

object QuizCountdownFeature : SwitchFeature(
    name = "Quiz Countdown",
    description = "Shows a countdown until the next quiz question is ready",
    category = "Dungeons",
    configKey = "quiz_countdown_hud",
    subcategory = "Clear",
) {

    private const val FIRST_QUESTION_DELAY_MS = 12000L
    private const val READY_DELAY_MS = 7000L

    private var countdownEndAt: Long? = null

    private val startRegex = Regex("^\\[STATUE] Oruo the Omniscient: I am Oruo the Omniscient\\. I have lived many lives\\. I have learned all there is to know\\.$")
    private val q1AnsweredRegex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered Question #1 correctly!$")
    private val q2AnsweredRegex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered Question #2 correctly!$")
    private val finalAnsweredRegex = Regex("^\\[STATUE] Oruo the Omniscient: .+ answered the final question correctly!$")

    private val element = object : HudElement(
        id = "quiz_countdown",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.55f,
    ) {
        override val shadow = true

        override fun content(): List<String> {
            val endAt = countdownEndAt ?: return emptyList()
            val remainingMs = endAt - System.currentTimeMillis()

            if (remainingMs <= 0) {
                countdownEndAt = null
                return emptyList()
            }

            val seconds = remainingMs / 1000
            val hundredths = (remainingMs % 1000) / 10

            return listOf("§b§lNext Question: §f§l${seconds}.${"%02d".format(hundredths)}s")
        }

        override fun preview(): List<String> = listOf("§b§lNext Question: §f§l4.00s")
    }

    init {
        HudManager.register(element)

        onChat(DungeonState.state) { text, _, _ ->
            when {
                startRegex.matches(text) -> countdownEndAt = System.currentTimeMillis() + FIRST_QUESTION_DELAY_MS
                q1AnsweredRegex.matches(text) -> countdownEndAt = System.currentTimeMillis() + READY_DELAY_MS
                q2AnsweredRegex.matches(text) -> countdownEndAt = System.currentTimeMillis() + READY_DELAY_MS
                finalAnsweredRegex.matches(text) -> countdownEndAt = null
            }
        }
    }
}