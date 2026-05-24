package io.github.meridian.features.impl

import io.github.meridian.features.ColorFeature

object TestColor : ColorFeature(
    name = "Test Color",
    description = "Pick a color — saved per-feature.",
    category = "Events",
    configKey = "test_color",
    subcategory = "Diana",
    defaultColor = 0xFFBB86FC.toInt(),
)