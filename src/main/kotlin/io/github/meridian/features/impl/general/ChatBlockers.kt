package io.github.meridian.features.impl.general

import com.google.gson.JsonObject
import io.github.meridian.Meridian
import io.github.meridian.features.FeatureManager
import io.github.meridian.features.types.ButtonFeature
import io.github.meridian.gui.ChatBlockerScreen
import io.github.meridian.utils.BasicState
import io.github.meridian.utils.ChatBlockListener
import io.github.meridian.utils.DungeonState
import io.github.meridian.utils.State
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

// ============================================================================ //
//   ADDING A BLOCKER — edit the ENTRIES table near the bottom of this file.    //
//   Everything else here is plumbing.                                          //
// ============================================================================ //

/**
 * One toggleable chat-blocking rule. A message is blocked when *any* of [regex] matches, *any* of
 * [contains] is a substring, or [custom] returns true. [gate] optionally restricts the rule to a
 * location/phase state — while the gate is false the rule isn't even attached to the listener list.
 */
class ChatBlockerEntry(
    val id: String,
    val group: String,
    val label: String,
    val note: String = "",
    private val gate: State<Boolean>? = null,
    private val regex: List<Regex> = emptyList(),
    private val contains: List<String> = emptyList(),
    private val custom: ((Component) -> Boolean)? = null,
    defaultEnabled: Boolean = false,
) {
    val enabledState = BasicState(defaultEnabled)

    var enabled: Boolean
        get() = enabledState.value
        set(value) { enabledState.value = value }

    fun toggle() {
        enabled = !enabled
        FeatureManager.save()
    }

    private fun matches(message: Component): Boolean {
        val plain = message.string
        if (regex.any { it.containsMatchIn(plain) }) return true
        if (contains.any { plain.contains(it) }) return true
        return custom?.invoke(message) == true
    }

    internal fun attach() {
        val active = if (gate == null) enabledState else enabledState.zip(gate, Boolean::and)
        ChatBlockListener { message -> matches(message) }.bind(active)
    }
}

object ChatBlockerRegistry {
    /** Declaration order drives the GUI order; grouping is stable within it. */
    fun grouped(): Map<String, List<ChatBlockerEntry>> = ENTRIES.groupBy { it.group }

    fun init() = ENTRIES.forEach { it.attach() }

    fun saveTo(json: JsonObject) = ENTRIES.forEach { json.addProperty(it.id, it.enabled) }

    fun loadFrom(json: JsonObject) = ENTRIES.forEach { e ->
        if (json.has(e.id)) e.enabled = json.get(e.id).asBoolean
    }

    /**
     * Pre-unification each blocker was its own [io.github.meridian.features.types.SwitchFeature],
     * saved under `features/<category>/<subcategory>/<configKey>/enabled`. Entry ids deliberately
     * reuse those configKeys, so a one-time walk of the old node carries settings over.
     * Only runs when the `chatBlockers` node is absent (i.e. first launch after the change).
     */
    fun migrateLegacy(featuresNode: JsonObject) {
        val byId = ENTRIES.associateBy { it.id }
        fun walk(node: JsonObject) {
            for ((key, value) in node.entrySet()) {
                if (!value.isJsonObject) continue
                val obj = value.asJsonObject
                val entry = byId[key]
                if (entry != null) {
                    if (obj.has("enabled")) entry.enabled = obj.get("enabled").asBoolean
                } else {
                    walk(obj)
                }
            }
        }
        walk(featuresNode)
    }
}

object ChatBlockerButton : ButtonFeature(
    name = "Chat Blockers",
    description = "Pick which Hypixel chat messages get hidden from your chat.",
    category = "General",
    configKey = "chat_blocker_button",
    subcategory = "Chat Blockers",
    buttonLabel = "Open GUI",
    onClick = {
        Meridian.mc.execute { Meridian.mc.setScreen(ChatBlockerScreen()) }
    }
)

private const val GENERAL = "General"
private const val DUNGEONS = "Dungeons"

private fun blocker(
    id: String,
    group: String,
    label: String,
    note: String = "",
    gate: State<Boolean>? = null,
    regex: List<String> = emptyList(),
    contains: List<String> = emptyList(),
    custom: ((Component) -> Boolean)? = null,
    defaultEnabled: Boolean = false,
) = ChatBlockerEntry(id, group, label, note, gate, regex.map(::Regex), contains, custom, defaultEnabled)

// ============================================================================ //
//                              THE BLOCKER TABLE                               //
//                                                                              //
//  `id` is the persistence key — stable, never rename it.                      //
//  `label` is the line shown in the GUI; write it as the message players see.  //
//  `note` is optional grey subtext for anything the label can't convey.        //
//  `gate` restricts a rule to a location/phase (e.g. DungeonState.state).      //
// ============================================================================ //

private val ENTRIES: List<ChatBlockerEntry> = listOf(

    // ------------------------------- General -------------------------------- //

    blocker(
        id = "block_blocks_in_way",
        group = GENERAL,
        label = "There are blocks in the way!",
        regex = listOf("^There are blocks in the way!$"),
    ),
    blocker(
        id = "block_gexp",
        group = GENERAL,
        label = "You earned 100 GEXP + 20 Event EXP from playing SkyBlock!",
        regex = listOf("^You earned .+ from playing SkyBlock!$"),
    ),
    blocker(
        id = "block_profile_id",
        group = GENERAL,
        label = "Profile ID: 00000000-0000-0000-0000-000000000000",
        regex = listOf("^Profile ID: .+$"),
    ),
    blocker(
        id = "block_profile_produce",
        group = GENERAL,
        label = "You are playing on profile: Mango",
        note = "Also hides the (Co-op) variant.",
        regex = listOf("^You are playing on profile: "),
    ),
    blocker(
        id = "block_hotfm",
        group = GENERAL,
        label = "You can disable this messaging by toggling Lottery/Sky Mall in your /hotf!",
        regex = listOf("^You can disable this messaging by toggling (Lottery|Sky Mall) in your (/hotf!|/hotm!)$"),
    ),
    blocker(
        id = "block_discord",
        group = GENERAL,
        label = "Please be mindful of Discord links in chat as they may pose a security risk",
        custom = { message -> DiscordWarning.strip(message) },
    ),
    blocker(
        id = "block_watchdog",
        group = GENERAL,
        label = "[WATCHDOG ANNOUNCEMENT] Watchdog has banned 9,999 players in the last 7 days.",
        note = "Also hides the staff-ban and blacklisted-modifications lines.",
        regex = listOf(
            "^\\[WATCHDOG ANNOUNCEMENT\\]$",
            "^Watchdog has banned .* players in the last 7 days\\.$",
            "^Staff have banned an additional .* in the last 7 days\\.$",
            "^Blacklisted modifications are a bannable offense!$",
        ),
    ),

    // ------------------------------- Dungeons ------------------------------- //

    blocker(
        id = "block_pf_warning",
        group = DUNGEONS,
        label = "Clicking sketchy links can result in your account being stolen!",
        note = "The whole Party Finder security warning block.",
        contains = listOf(
            "  Clicking sketchy links can result in your account",
            "  being stolen!",
            "  Link looks suspicious? - Don't click it!",
        ),
        regex = listOf("^   $"),
    ),
    blocker(
        id = "block_blessings",
        group = DUNGEONS,
        label = "Player has obtained Blessing of Power!",
        note = "Also hides the DUNGEON BUFF! and \"granted you +\" follow-ups.",
        gate = DungeonState.state,
        regex = listOf(
            "has obtained Blessing of \\w+!$",
            "^DUNGEON BUFF! ",
            "^\\s*(?:Also )?[Gg]ranted you \\+",
        ),
    ),
    blocker(
        id = "block_superboom",
        group = DUNGEONS,
        label = "Player has obtained Superboom TNT!",
        gate = DungeonState.state,
        regex = listOf("(\\S+) has obtained Superboom TNT!$"),
    ),
    blocker(
        id = "block_revive_stone",
        group = DUNGEONS,
        label = "Player has obtained Revive Stone!",
        gate = DungeonState.state,
        regex = listOf("(\\S+) has obtained Revive Stone!$"),
    ),
    blocker(
        id = "block_beating_heart",
        group = DUNGEONS,
        label = "Player has obtained Beating Heart!",
        gate = DungeonState.state,
        regex = listOf("(\\S+) has obtained Beating Heart!"),
    ),
    blocker(
        id = "block_trap",
        group = DUNGEONS,
        label = "The Spike Trap hit you for 500 damage!",
        gate = DungeonState.state,
        regex = listOf("^The .+ hit you for .+ damage!$"),
    ),
    blocker(
        id = "watcher_yap_hider",
        group = DUNGEONS,
        label = "[BOSS] The Watcher: That one was weak anyway.",
        note = "Hides 14 of the Watcher's filler lines.",
        custom = { message -> WatcherYap.matches(message.string) },
    ),
)

// --- Rules too involved for the table above. ---

private object DiscordWarning {
    private const val WARNING = "Please be mindful of Discord links in chat as they may pose a security risk"

    /**
     * Hypixel appends the warning to whatever message contained the link, so a plain veto would eat
     * the real message too. Re-adds the message with the warning (and its trailing blanks) removed,
     * then reports a block for the original.
     */
    fun strip(message: Component): Boolean {
        if (WARNING !in message.string) return false
        val rebuilt: MutableComponent =
            MutableComponent.create(message.contents).setStyle(message.style)
        if (WARNING in rebuilt.string) return true
        val kept = mutableListOf<Component>()
        for (sib in message.siblings) {
            if (WARNING in sib.string) break
            kept += sib
        }
        while (kept.isNotEmpty() && kept.last().string.isBlank()) kept.removeAt(kept.size - 1)
        kept.forEach { rebuilt.append(it) }
        if (rebuilt.string.isNotBlank()) Meridian.mc.gui.chat.addClientSystemMessage(rebuilt)
        return true
    }
}

private object WatcherYap {
    private const val PREFIX = "[BOSS] The Watcher: "

    private val lines = setOf(
        "That one was weak anyway.",
        "Things feel a little more roomy now, eh?",
        "I've knocked down those pillars to go for a more...open concept.",
        "Plus I needed to give my new friends some space to roam...",
        "This guy looks like a fighter.",
        "I'm impressed.",
        "Hmmm... this one!",
        "Ouch.. just kidding.",
        "Go, fight!",
        "Very nice.",
        "Aw, I liked that one.",
        "Go and live again!",
        "You'll do.",
        "Not bad.",
    )

    fun matches(msg: String): Boolean =
        msg.startsWith(PREFIX) && msg.removePrefix(PREFIX).trim() in lines
}
