package io.github.meridian.features.impl.dungeons

import io.github.meridian.Meridian.mc
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.TickScheduler
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import kotlin.random.Random

// Play music at end of doogan
object DungeonEndMusic : SwitchFeature(
    name = "Dungeon End Music",
    description = "Plays a random music disc after run completion :D",
    category = "Dungeons",
    configKey = "dungeon_end_music",
    subcategory = "Miscellaneous"
) {
    // Display name -> disc sound. Mirrors the CT "records.<name>" list. The
    // MUSIC_DISC_* fields are Holder.Reference<SoundEvent>, so unwrap with value().
    private val DISCS: List<Pair<String, SoundEvent>> = listOf(
        "mall" to SoundEvents.MUSIC_DISC_MALL.value(),
        "blocks" to SoundEvents.MUSIC_DISC_BLOCKS.value(),
        "cat" to SoundEvents.MUSIC_DISC_CAT.value(),
        "chirp" to SoundEvents.MUSIC_DISC_CHIRP.value(),
        "far" to SoundEvents.MUSIC_DISC_FAR.value(),
        "mellohi" to SoundEvents.MUSIC_DISC_MELLOHI.value(),
        "stal" to SoundEvents.MUSIC_DISC_STAL.value(),
        "strad" to SoundEvents.MUSIC_DISC_STRAD.value(),
        "wait" to SoundEvents.MUSIC_DISC_WAIT.value(),
        "ward" to SoundEvents.MUSIC_DISC_WARD.value(),
    )

    // Matches the end-of-run summary line, e.g. "Team Score: 305 (S+)".
    private val TEAM_SCORE = Regex("^ *Team Score: (\\d+) \\(([\\w+]{1,2})\\)$")

    // True once we've played a disc for the current run (reset on the next run /
    // world change), so the disc plays exactly once per run.
    private var soundtrackPlayed = false

    // The disc we're currently playing, kept so we can stop it on world change.
    private var currentSound: SoundInstance? = null

    // Last seen client level; a change of identity (including to null) means we
    // left / swapped worlds — the CT "worldUnload" equivalent.
    private var lastLevel: ClientLevel? = null

    init {
        // End-of-run summary: play a random disc once.
        onChatMessage { text, _, _ ->
            if (!enabled || soundtrackPlayed) return@onChatMessage
            if (!TEAM_SCORE.matches(text)) return@onChatMessage

            val (name, sound) = DISCS[Random.nextInt(DISCS.size)]
            // RECORDS category so the "Jukebox/Note Blocks" volume slider applies.
            // relative=true + Attenuation.NONE keeps it non-positional (same volume
            // wherever the player moves), like forUI but on a controllable channel.
            val instance = SimpleSoundInstance(
                sound.location,
                SoundSource.RECORDS,
                1.0f, 1.0f,
                RandomSource.create(),
                false, 0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
            )
            currentSound = instance
            mc.soundManager.play(instance)

            // CT delayed the chat line by 200ms; ~4 client ticks here.
            TickScheduler.schedule(4, serverTick = false) {
                modMessage("§fNow enjoying $name")
            }
            soundtrackPlayed = true
        }

        // New run starting: re-arm so the next run plays again.
        onChatMessage { text, _, _ ->
            if (text == "Starting in 1 second.") soundtrackPlayed = false
        }

        // World change (CT "worldUnload"): stop the disc and re-arm.
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            val level = mc.level
            if (level !== lastLevel) {
                lastLevel = level
                soundtrackPlayed = false
                currentSound?.let { mc.soundManager.stop(it) }
                currentSound = null
            }
        })
    }
}
