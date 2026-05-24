package io.github.meridian.features
// ALL NEW FEATURES NEED TO BE IMPORTED BELOW IN ADDITION TO BEING ADDED TO THE FeatureList!
import io.github.meridian.features.impl.TestButton
import io.github.meridian.features.impl.TestColor
import io.github.meridian.features.impl.TestSwitch
import io.github.meridian.features.impl.TestText


// Manifest of every feature in the mod — the only place that needs updating
// when a new feature file is added. Similar in spirit to a ChatTriggers index.js.
object FeatureList {
    private val all: List<Feature> = listOf(
        TestSwitch,
        TestButton,
        TestColor,
        TestText,
        // add new features here
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}