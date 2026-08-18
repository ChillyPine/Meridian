package io.github.meridian.features

import io.github.meridian.features.impl.dungeons.AnnounceShitter
import io.github.meridian.features.impl.dungeons.AutoKickShitter
import io.github.meridian.features.impl.dungeons.BoxBats
import io.github.meridian.features.impl.dungeons.BoxBatsColor
import io.github.meridian.features.impl.dungeons.BatTracer
import io.github.meridian.features.impl.dungeons.BloodCleared
import io.github.meridian.features.impl.dungeons.BloodFull
import io.github.meridian.features.impl.dungeons.BloodNotifs
import io.github.meridian.features.impl.dungeons.BloodOpen
import io.github.meridian.features.impl.dungeons.BottleFull
import io.github.meridian.features.impl.dungeons.CustomShitterMessage
import io.github.meridian.features.impl.dungeons.BoxDoorKeys
import io.github.meridian.features.impl.dungeons.DoorKeyColor
import io.github.meridian.features.impl.dungeons.DungeonEndMusic
import io.github.meridian.features.impl.dungeons.FFTimer
//import io.github.meridian.features.impl.dungeons.LavaBounceFix
import io.github.meridian.features.impl.dungeons.LividHealthHUD
import io.github.meridian.features.impl.dungeons.FelColor
import io.github.meridian.features.impl.dungeons.BoxFels
import io.github.meridian.features.impl.dungeons.GoldorColor
import io.github.meridian.features.impl.dungeons.BoxGoldor
import io.github.meridian.features.impl.dungeons.LockedChestNotif
import io.github.meridian.features.impl.dungeons.M5WishNotif
import io.github.meridian.features.impl.dungeons.MaskUsed
import io.github.meridian.features.impl.dungeons.P1WishNotif
import io.github.meridian.features.impl.dungeons.P3WishNotif
import io.github.meridian.features.impl.dungeons.P4Platform
import io.github.meridian.features.impl.dungeons.PadHelper
import io.github.meridian.features.impl.dungeons.PartyActions
import io.github.meridian.features.impl.dungeons.PlayProcSound
import io.github.meridian.features.impl.dungeons.PlaySoundOnKeyDrop
import io.github.meridian.features.impl.dungeons.SendBloodToParty
import io.github.meridian.features.impl.dungeons.SendMaskInPartyChat
import io.github.meridian.features.impl.dungeons.ShadowAssassinColor
import io.github.meridian.features.impl.dungeons.BoxShadowAssassins
import io.github.meridian.features.impl.dungeons.BoxSpiritBear
import io.github.meridian.features.impl.dungeons.ShitterListButton
import io.github.meridian.features.impl.dungeons.ShortPFMessage
import io.github.meridian.features.impl.dungeons.SimonSaysPC
import io.github.meridian.features.impl.dungeons.SimonSaysTime
import io.github.meridian.features.impl.dungeons.StarMobColor
import io.github.meridian.features.impl.dungeons.BoxStarMobs
import io.github.meridian.features.impl.dungeons.StormColor
import io.github.meridian.features.impl.dungeons.BoxStorm
import io.github.meridian.features.impl.dungeons.CarryManagerButton
import io.github.meridian.features.impl.dungeons.CooldownTimerHUD
import io.github.meridian.features.impl.dungeons.CustomLeapMessageTXT
import io.github.meridian.features.impl.dungeons.CustomLeapMessage
import io.github.meridian.features.impl.dungeons.HoldingCrystal
import io.github.meridian.features.impl.dungeons.HoldingRelic
import io.github.meridian.features.impl.dungeons.M6WishNotif
import io.github.meridian.features.impl.dungeons.QuizCountdownFeature
import io.github.meridian.features.impl.dungeons.QuizFeatures
import io.github.meridian.features.impl.dungeons.RelicHelper
import io.github.meridian.features.impl.dungeons.SendShitterReason
import io.github.meridian.features.impl.dungeons.StackHelper
import io.github.meridian.features.impl.events.BoxPrimalFears
import io.github.meridian.features.impl.events.JerryNotif
import io.github.meridian.features.impl.events.QuickMathsSolver
import io.github.meridian.features.impl.farming.BoxTrapperMobs
import io.github.meridian.features.impl.carryhelper.AnnounceProgressClient
import io.github.meridian.features.impl.carryhelper.DontCountDeaths
import io.github.meridian.features.impl.carryhelper.AutoDetectTrade
import io.github.meridian.features.impl.carryhelper.AutoTrackClientProgress
import io.github.meridian.features.impl.carryhelper.AutoTrackDungeonProgress
import io.github.meridian.features.impl.carryhelper.BoxClientsBosses
import io.github.meridian.features.impl.carryhelper.DrawLineToClientBoss
import io.github.meridian.features.impl.carryhelper.HighlightClients
import io.github.meridian.features.impl.carryhelper.TrackClientKillTime
import io.github.meridian.features.impl.carryhelper.TrackClientSpawnTime
import io.github.meridian.features.impl.carryhelper.TrackSessionTime
import io.github.meridian.features.impl.dungeons.ArchCritChecker
import io.github.meridian.features.impl.dungeons.ArchGyroWaypoint
import io.github.meridian.features.impl.dungeons.SuperBounceHUD
import io.github.meridian.features.impl.foraging.BeeheemothQuickWarp
import io.github.meridian.features.impl.foraging.BeeheemothWaypoints
import io.github.meridian.features.impl.foraging.BoxBloodbat
import io.github.meridian.features.impl.foraging.BoxPangolins
import io.github.meridian.features.impl.foraging.HoneyhiveWaypoints
import io.github.meridian.features.impl.foraging.BoxShinyCritters
import io.github.meridian.features.impl.foraging.BoxShulkers
import io.github.meridian.features.impl.foraging.ShinyCritterColor
import io.github.meridian.features.impl.foraging.ShulkerColor
import io.github.meridian.features.impl.general.CoordsCommand
import io.github.meridian.features.impl.general.IRLClockColor
import io.github.meridian.features.impl.general.IRLTime
import io.github.meridian.features.impl.general.DTCommand
import io.github.meridian.features.impl.general.BoxFemboys
import io.github.meridian.features.impl.general.ChatBlockerButton
import io.github.meridian.features.impl.general.BoxMatchos
import io.github.meridian.features.impl.general.MatchoColor
import io.github.meridian.features.impl.general.NonRemover
import io.github.meridian.features.impl.general.BoxOldWolves
import io.github.meridian.features.impl.general.OldWolfColor
import io.github.meridian.features.impl.vanilla.PlayerNametag
import io.github.meridian.features.impl.general.PLActions
import io.github.meridian.features.impl.general.PLMoreActions
import io.github.meridian.features.impl.general.BoxPlayers
import io.github.meridian.features.impl.general.PlayerBoxMode
import io.github.meridian.features.impl.general.BoxSpecificPlayer
import io.github.meridian.features.impl.general.BoxRats
import io.github.meridian.features.impl.vanilla.RemoveNausea
import io.github.meridian.features.impl.vanilla.RemoveRealms
import io.github.meridian.features.impl.general.RunicMobColor
import io.github.meridian.features.impl.general.BoxRunicMobs
import io.github.meridian.features.impl.general.LatestGHAction
import io.github.meridian.features.impl.general.LoadoutHudFeature
import io.github.meridian.features.impl.vanilla.RemoveChatBar
import io.github.meridian.features.impl.general.RunicMobTracer
import io.github.meridian.features.impl.general.SoundListButton
import io.github.meridian.features.impl.general.UpdateChecker
import io.github.meridian.features.impl.mining.BoxButterflies
import io.github.meridian.features.impl.mining.ButterflyTracer
import io.github.meridian.features.impl.mining.BoxCorleone
import io.github.meridian.features.impl.mining.BoxDiamondGoblins
import io.github.meridian.features.impl.mining.BoxGoldenGoblins
import io.github.meridian.features.impl.mining.BoxKeyGuardians
import io.github.meridian.features.impl.vanilla.ChatEraser
import org.apache.commons.lang3.arch.Processor

// Manifest of every feature in the mod — the only place that needs updating
// when a new feature file is added. Similar in spirit to ChatTriggers' index.js.
// ORDER MATTERS - KEEP THINGS IN THE CORRECT ORDER!
object FeatureList {
    private val all: List<Feature> = listOf(

        // ================================================== //
        //                      GENERAL                       //
        // ================================================== //

        // --- Boxes ---
        BoxRunicMobs,
        RunicMobTracer,
        RunicMobColor,
        BoxMatchos,
        MatchoColor,
        BoxOldWolves,
        OldWolfColor,
        BoxRats,
        BoxPlayers,
        PlayerBoxMode,
        BoxSpecificPlayer,
        BoxFemboys,

        // --- ! Commands ---
        DTCommand,
        CoordsCommand,
        // PTCommand, <- NOT IMPLEMENTED

        // --- Chat Blockers ---
        // Individual blockers live in the ENTRIES table in impl/general/ChatBlockers.kt,
        // not here — this button opens their GUI.
        ChatBlockerButton,

        // --- Party ---
        PLActions,
        PLMoreActions,

        // --- Miscellaneous ---
        IRLTime,
        IRLClockColor,
        LoadoutHudFeature,
        NonRemover,
        SoundListButton,
        UpdateChecker,
        LatestGHAction,
        // ================================================== //
        //                     DUNGEONS                       //
        // ================================================== //

        // --- Clear ---
        BoxStarMobs,
        StarMobColor,
        BoxFels,
        FelColor,
        BoxShadowAssassins,
        ShadowAssassinColor,
        BoxBats,
        BatTracer,
        BoxBatsColor,
        LockedChestNotif,
        BoxDoorKeys,
        DoorKeyColor,
        PlaySoundOnKeyDrop,
        QuizFeatures,
        QuizCountdownFeature,
        BloodNotifs,
        BloodOpen,
        BloodFull,
        BloodCleared,
        SendBloodToParty,

        // --- P1 ---
        HoldingCrystal,
        P1WishNotif,
        ArchGyroWaypoint,

        // --- P2 ---
        BoxStorm,
        StormColor,
        PadHelper,
        ArchCritChecker,

        // --- P3 ---
        SuperBounceHUD,
        P3WishNotif,
        BoxGoldor,
        GoldorColor,
        SimonSaysTime,
        SimonSaysPC,

        // --- P4 ---
        P4Platform,

        // --- P5 ---
        RelicHelper,
        HoldingRelic,
        StackHelper,

        // --- M3 ---
        FFTimer,

        // --- M4 ---
        BoxSpiritBear,

        // --- M5 ---
        LividHealthHUD,
        M5WishNotif,

        // --- M6 ---
        M6WishNotif,

        // --- Misc ---
        //LavaBounceFix,
        BottleFull,
        MaskUsed,
        PlayProcSound,
        SendMaskInPartyChat,
        DungeonEndMusic,
        CooldownTimerHUD,
        ShortPFMessage,
        PartyActions,
        ShitterListButton,
        AutoKickShitter,
        AnnounceShitter,
        CustomShitterMessage,
        SendShitterReason,
        CustomLeapMessage,
        CustomLeapMessageTXT,

        // ================================================== //
        //                      FARMING                       //
        // ================================================== //

        BoxTrapperMobs,

        // ================================================== //
        //                      FORAGING                      //
        // ================================================== //

        BoxShulkers,
        ShulkerColor,
        BoxPangolins,
        HoneyhiveWaypoints,
        BeeheemothWaypoints,
        BeeheemothQuickWarp,
        BoxBloodbat,
        BoxShinyCritters,
        ShinyCritterColor,

        // ================================================== //
        //                      MINING                        //
        // ================================================== //

        BoxButterflies,
        ButterflyTracer,
        BoxCorleone,
        BoxGoldenGoblins,
        BoxDiamondGoblins,
        BoxKeyGuardians,

        // ================================================== //
        //                      EVENTS                        //
        // ================================================== //

        BoxPrimalFears,
        QuickMathsSolver,
        JerryNotif,

        // ================================================== //
        //                      Carry Helper                  //
        // ================================================== //

        // --- General ---
        CarryManagerButton,
        HighlightClients,
        BoxClientsBosses,
        DrawLineToClientBoss,
        AutoTrackClientProgress,
        AnnounceProgressClient,
        DontCountDeaths,
        TrackClientSpawnTime,
        TrackClientKillTime,
        TrackSessionTime,
        AutoTrackDungeonProgress,
        AutoDetectTrade,

        // ================================================== //
        //                      VANILLA                       //
        // ================================================== //

        // --- Tweaks ---
        RemoveRealms,
        RemoveNausea,
        RemoveChatBar,
        PlayerNametag,

        // --- Miscellaneous ---
        ChatEraser,
    )

    fun registerAll() {
        all.forEach(FeatureManager::register)
    }
}