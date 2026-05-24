package io.github.meridian.features.impl

import io.github.meridian.features.TextFeature

object TestText : TextFeature(
    name = "Test Text",
    description = "Free-form string.",
    category = "general",
    configKey = "test_text",
    subcategory = "Testing",
    placeholder = "Input here",
    maxLength = 128,
)