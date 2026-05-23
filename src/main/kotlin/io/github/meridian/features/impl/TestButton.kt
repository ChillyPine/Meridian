package io.github.meridian.features.impl

import io.github.meridian.features.ButtonFeature
import io.github.meridian.utils.sendClientMessage
import io.github.meridian.utils.sendCommand



object TestButton : ButtonFeature(
    name = "Test Button",
    description = "Verify the feature system works.",
    category = "general",
    configKey = "test_switch",
    subcategory = "Testing",
    buttonLabel = "Open",
    onClick = { sendClientMessage("Test Button Clicked!") },
)