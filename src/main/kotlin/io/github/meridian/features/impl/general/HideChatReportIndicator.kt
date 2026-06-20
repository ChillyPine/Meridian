package io.github.meridian.features.impl.general

import io.github.meridian.features.types.SwitchFeature

// Code is in a Mixin (or two).
object RemoveChatBar : SwitchFeature(
    name = "Remove White Chat Bar",
    description = "Removes the white bar on the left side of chat and removes the indent.",
    category = "Vanilla",
    configKey = "remove_chat_bar",
    subcategory = "Tweaks",
)
