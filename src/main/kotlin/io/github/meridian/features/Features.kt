package io.github.meridian.features

import io.github.meridian.features.impl.TestSwitch

// Manifest of every feature in the mod — the only place that needs updating
// when a new feature file is added. Similar in spirit to a ChatTriggers index.js.
object Features {
    private val all: List<Feature> = listOf(
        TestSwitch,
        // add new features here
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}
