package io.github.meridian.features.impl

import io.github.meridian.features.SwitchFeature

// Example feature. Each feature lives in its own file so its behavior
// hooks (event listeners, chat handlers, etc.) can be added alongside
// the declaration when needed.
object TestSwitch : SwitchFeature(
    name = "Test Switch",
    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam",
    category = "General",
    configKey = "test_switch",
    subcategory = "Testing"
)

