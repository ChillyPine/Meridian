package io.github.meridian.features.impl

import io.github.meridian.features.ButtonFeature
import io.github.meridian.utils.modMessage

object TestButton : ButtonFeature(
    name = "Test Button",
    description = "Verify the feature system works.",
    category = "General",
    configKey = "test_button",
    subcategory = "Testing",
    buttonLabel = "Click",
    onClick = {
        modMessage("Test button clicked.")
    },
)