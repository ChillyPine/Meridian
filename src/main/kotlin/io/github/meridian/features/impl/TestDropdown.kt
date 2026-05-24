package io.github.meridian.features.impl

import io.github.meridian.features.DropdownFeature
import io.github.meridian.utils.modMessage

object TestDropdown : DropdownFeature(
    name = "Test Dropdown",
    description = "Verify the dropdown feature works.",
    category = "General",
    configKey = "test_dropdown",
    subcategory = "Testing",
    options = listOf("Option A", "Option B", "Option C"),
    defaultIndex = 0,
    onChange = { selected ->
        modMessage("Dropdown changed to: $selected")
    },
)