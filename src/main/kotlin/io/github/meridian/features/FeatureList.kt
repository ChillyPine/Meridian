package io.github.meridian.features
// ALL NEW FEATURES NEED TO BE IMPORTED BELOW IN ADDITION TO BEING ADDED TO THE FeatureList!
// Tab autocomplete adds imports automatically
import io.github.meridian.features.impl.dungeons.BatESP
import io.github.meridian.features.impl.dungeons.BatESPColor
import io.github.meridian.features.impl.dungeons.BatTracer
import io.github.meridian.features.impl.dungeons.BlockPFWarning
import io.github.meridian.features.impl.dungeons.BloodCleared
import io.github.meridian.features.impl.dungeons.BloodFull
import io.github.meridian.features.impl.dungeons.BloodNotifs
import io.github.meridian.features.impl.dungeons.BloodOpen
import io.github.meridian.features.impl.dungeons.DungeonEndMusic
import io.github.meridian.features.impl.dungeons.FFTimer
import io.github.meridian.features.impl.dungeons.LividHealthHUD
import io.github.meridian.features.impl.dungeons.FelColor
import io.github.meridian.features.impl.dungeons.FelESP
import io.github.meridian.features.impl.dungeons.GoldorColor
import io.github.meridian.features.impl.dungeons.GoldorESP
import io.github.meridian.features.impl.dungeons.LockedChestNotif
import io.github.meridian.features.impl.dungeons.M5WishNotif
import io.github.meridian.features.impl.dungeons.P1WishNotif
import io.github.meridian.features.impl.dungeons.P3WishNotif
import io.github.meridian.features.impl.dungeons.SendBloodToParty
import io.github.meridian.features.impl.dungeons.ShadowAssassinColor
import io.github.meridian.features.impl.dungeons.ShadowAssassinESP
import io.github.meridian.features.impl.dungeons.ShortPFMessage
import io.github.meridian.features.impl.dungeons.StarMobColor
import io.github.meridian.features.impl.dungeons.StarMobESP
import io.github.meridian.features.impl.dungeons.StormColor
import io.github.meridian.features.impl.dungeons.StormESP
import io.github.meridian.features.impl.dungeons.WatcherYapHider
import io.github.meridian.features.impl.events.PrimalFearESP
import io.github.meridian.features.impl.events.QuickMathsSolver
import io.github.meridian.features.impl.farming.TrapperESP
import io.github.meridian.features.impl.general.BlockBlocksInWay
import io.github.meridian.features.impl.general.BlockDiscord
import io.github.meridian.features.impl.general.BlockGEXP
import io.github.meridian.features.impl.general.BlockHOTFM
import io.github.meridian.features.impl.general.BlockProfileID
import io.github.meridian.features.impl.general.BlockProfileProduce
import io.github.meridian.features.impl.general.CoordsCommand
import io.github.meridian.features.impl.general.IRLClockColor
import io.github.meridian.features.impl.general.IRLTime
import io.github.meridian.features.impl.general.DTCommand
import io.github.meridian.features.impl.general.FemboyESP
import io.github.meridian.features.impl.general.MatchoESP
import io.github.meridian.features.impl.general.MatchoESPColor
import io.github.meridian.features.impl.general.OldWolfESP
import io.github.meridian.features.impl.general.OldWolfESPColor
import io.github.meridian.features.impl.general.PLActions
import io.github.meridian.features.impl.general.PLMoreActions
import io.github.meridian.features.impl.general.PlayerESP
import io.github.meridian.features.impl.general.PlayerESPMode
import io.github.meridian.features.impl.general.SpecificPlayerESP
import io.github.meridian.features.impl.general.RatESP
import io.github.meridian.features.impl.general.RemoveNausea
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
        RatESP,
        PlayerESP,
        PlayerESPMode,
        SpecificPlayerESP,
        FemboyESP,
        DTCommand,
        CoordsCommand,
        //PTCommand,
        BlockBlocksInWay,
        BlockGEXP,
        BlockProfileID,
        BlockProfileProduce,
        BlockHOTFM,
        BlockDiscord,
        PLActions,
        PLMoreActions,
        IRLTime,
        IRLClockColor,
        RemoveNausea,
        SoundListButton,
        // Dungeons
        StarMobESP,
        StarMobColor,
        FelESP,
        FelColor,
        ShadowAssassinESP,
        ShadowAssassinColor,
        BatESP,
        BatTracer,
        BatESPColor,
        LockedChestNotif,
        BloodNotifs,
        BloodOpen,
        BloodFull,
        BloodCleared,
        SendBloodToParty,
        WatcherYapHider,
        P1WishNotif,
        StormESP,
        StormColor,
        P3WishNotif,
        GoldorESP,
        GoldorColor,
        FFTimer,
        //LividHealthHUD,
        M5WishNotif,
        DungeonEndMusic,
        ShortPFMessage,
        BlockPFWarning,
        // Farming
        TrapperESP,
        // Mining
        ButterflyESP,
        ButterflyTracer,
        CorleoneESP,
        GoldenGoblinESP,
        DiamondGoblinESP,
        KeyGuardianESP,
        // Events
        PrimalFearESP,
        QuickMathsSolver
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}