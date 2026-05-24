package io.github.meridian.features.impl

import io.github.meridian.features.ColorFeature

object TestColor : ColorFeature(
    name = "Test Color",
    description = "Pick a color — saved per-feature.",
    category = "General",
    configKey = "test_color",
    subcategory = "Testing",
    defaultColor = 0xFFBB86FC.toInt(),
    dependsOn = TestSwitch2
)