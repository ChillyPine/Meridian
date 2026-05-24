package io.github.meridian.features.impl

import io.github.meridian.features.SwitchFeature

object TestSwitch2 : SwitchFeature (
    name = "Other test",
    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam",
    category = "General",
    configKey = "other_test",
    subcategory = "Testing",
    dependsOn = TestSwitch
)