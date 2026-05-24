package io.github.meridian.features.impl

import io.github.meridian.features.SwitchFeature

// Example feature. Each feature lives in its own file so its behavior
// hooks (event listeners, chat handlers, etc.) can be added alongside
// the declaration when needed.
object TestSwitch : SwitchFeature(
    name = "Test Switch",
    description = "Verify the feature system works.",
    category = "General",
    configKey = "test_switch",
    subcategory = "Testing"
)

