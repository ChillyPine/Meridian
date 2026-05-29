package io.github.meridian.features
// ALL NEW FEATURES NEED TO BE IMPORTED BELOW IN ADDITION TO BEING ADDED TO THE FeatureList!
// Tab autocomplete adds imports automatically
import io.github.meridian.features.impl.dungeons.BlockPFWarning
import io.github.meridian.features.impl.dungeons.FFTimer
import io.github.meridian.features.impl.dungeons.FelColor
import io.github.meridian.features.impl.dungeons.FelESP
import io.github.meridian.features.impl.dungeons.ShadowAssassinColor
import io.github.meridian.features.impl.dungeons.ShadowAssassinESP
import io.github.meridian.features.impl.dungeons.StarMobColor
import io.github.meridian.features.impl.dungeons.StarMobESP
import io.github.meridian.features.impl.general.BlockBlocksInWay
import io.github.meridian.features.impl.general.BlockDiscord
import io.github.meridian.features.impl.general.BlockGEXP
import io.github.meridian.features.impl.general.BlockHOTFM
import io.github.meridian.features.impl.general.BlockProfileID
import io.github.meridian.features.impl.general.BlockProfileProduce
import io.github.meridian.features.impl.general.FemboyESP
import io.github.meridian.features.impl.general.MatchoESP
import io.github.meridian.features.impl.general.MatchoESPColor
import io.github.meridian.features.impl.general.OldWolfESP
import io.github.meridian.features.impl.general.OldWolfESPColor
import io.github.meridian.features.impl.general.RunicMobColor
import io.github.meridian.features.impl.general.RunicMobESP
import io.github.meridian.features.impl.general.RunicMobTracer
import io.github.meridian.features.impl.general.SoundListButton
import io.github.meridian.features.impl.mining.ButterflyESP
import io.github.meridian.features.impl.mining.ButterflyTracer
import io.github.meridian.features.impl.mining.CorleoneESP
import io.github.meridian.features.impl.mining.DiamondGoblinESP
import io.github.meridian.features.impl.mining.GoldenGoblinESP
import io.github.meridian.features.impl.mining.KeyGuardianESP


// Manifest of every feature in the mod — the only place that needs updating
// when a new feature file is added. Similar in spirit to a ChatTriggers index.js.
// ORDER MATTERS - KEEP THINGS IN THE CORRECT ORDER!
object FeatureList {
    private val all: List<Feature> = listOf(
        // General
        RunicMobESP,
        RunicMobTracer,
        RunicMobColor,
        MatchoESP,
        MatchoESPColor,
        OldWolfESP,
        OldWolfESPColor,
        FemboyESP,
        BlockBlocksInWay,
        BlockGEXP,
        BlockProfileID,
        BlockProfileProduce,
        BlockHOTFM,
        BlockDiscord,
        SoundListButton,
        // Dungeons
        StarMobESP,
        StarMobColor,
        FelESP,
        FelColor,
        ShadowAssassinESP,
        ShadowAssassinColor,
        FFTimer,
        BlockPFWarning,
        // Farming
        // Mining
        ButterflyESP,
        ButterflyTracer,
        CorleoneESP,
        GoldenGoblinESP,
        DiamondGoblinESP,
        KeyGuardianESP,
        // Events
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}