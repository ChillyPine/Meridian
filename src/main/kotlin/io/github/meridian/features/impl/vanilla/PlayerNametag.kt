package io.github.meridian.features.impl.vanilla

import io.github.meridian.features.types.SwitchFeature

// With this on, MixinLivingEntityRenderer forces
// shouldShowName true for the local player whenever the view isn't first-person,
// so your name floats above your head in F5 just like other players'.
object PlayerNametag : SwitchFeature(
    name = "Show Own Nametag",
    description = "Renders your own nametag above your head in third-person (F5) view.",
    category = "Vanilla",
    configKey = "player_nametag",
    subcategory = "Tweaks"
)