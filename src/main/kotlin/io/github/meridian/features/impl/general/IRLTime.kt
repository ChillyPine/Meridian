package io.github.meridian.features.impl.general

import io.github.meridian.features.types.ColorFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.hud.HudElement
import io.github.meridian.hud.HudManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object IRLTime : SwitchFeature(
    name = "IRL Time",
    description = "Displays your local time in a movable HUD element.\nUse /md hud to move the element.",
    category = "General",
    configKey = "irl_time",
    subcategory = "Miscellaneous",
) {
    // h:mm AM/PM, e.g. "3:05 PM" — same format as the original ChatTriggers clock.
    private val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    private fun now(): String = LocalTime.now().format(formatter)

    private val element = object : HudElement(
        id = "irl_time",
        name = name,
        defaultAnchorX = 0.5f,
        defaultAnchorY = 0.5f,
    ) {
        override val shadow = false
        override fun color(): Int = IRLClockColor.color
        override fun content(): List<String> = if (isActive()) listOf(now()) else emptyList()
        // The editor previews the live clock so its size/position are accurate.
        override fun preview(): List<String> = listOf(now())
    }

    init {
        HudManager.register(element)
    }
}

// Child of IRLTime: only visible/relevant while IRL Time is enabled.
object IRLClockColor : ColorFeature(
    name = "Clock Color",
    description = "Color of the IRL Time clock text.",
    category = "General",
    configKey = "irl_time_color",
    subcategory = "Miscellaneous",
    dependsOn = IRLTime,
)
