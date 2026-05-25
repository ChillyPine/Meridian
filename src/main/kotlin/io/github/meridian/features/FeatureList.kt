package io.github.meridian.features
// ALL NEW FEATURES NEED TO BE IMPORTED BELOW IN ADDITION TO BEING ADDED TO THE FeatureList!
// Tab autocomplete adds imports automatically
import io.github.meridian.features.impl.dungeons.BlockPFWarning
import io.github.meridian.features.impl.general.BlockBlocksInWay
import io.github.meridian.features.impl.general.BlockDiscord
import io.github.meridian.features.impl.general.BlockGEXP
import io.github.meridian.features.impl.general.BlockHOTFM
import io.github.meridian.features.impl.general.BlockProfileID
import io.github.meridian.features.impl.general.BlockProfileProduce
import io.github.meridian.features.impl.general.OldWolfESP
import io.github.meridian.features.impl.general.OldWolfESPColor
import io.github.meridian.features.impl.general.SoundListButton


// Manifest of every feature in the mod — the only place that needs updating
// when a new feature file is added. Similar in spirit to a ChatTriggers index.js.
object FeatureList {
    private val all: List<Feature> = listOf(
        BlockBlocksInWay,
        BlockGEXP,
        BlockProfileID,
        BlockProfileProduce,
        BlockHOTFM,
        BlockDiscord,
        SoundListButton,
        BlockPFWarning,
        OldWolfESP,
        OldWolfESPColor
        // add new features here
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}