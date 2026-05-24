package io.github.meridian.features.impl

import io.github.meridian.features.SwitchFeature

object TestSwitch2 : SwitchFeature (
    name = "Other test",
    description = "2 layers?",
    category = "General",
    configKey = "other_test",
    subcategory = "Testing",
    dependsOn = TestSwitch
)