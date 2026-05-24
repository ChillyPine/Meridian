package io.github.meridian.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

// Central registry for chat-message blockers. Each rule pairs an enabled-check
// (typically `{ SomeFeature.enabled }`) with a string predicate. A single
// ALLOW_GAME listener fans out to every rule — return false to cancel display.
object ChatBlocker {
    private data class Rule(val enabled: () -> Boolean, val matches: (String) -> Boolean)

    private val rules = mutableListOf<Rule>()

    fun register(enabled: () -> Boolean, matches: (String) -> Boolean) {
        rules += Rule(enabled, matches)
    }

    fun register(enabled: () -> Boolean, pattern: Regex) =
        register(enabled) { pattern.containsMatchIn(it) }

    fun register(enabled: () -> Boolean, substring: String) =
        register(enabled) { it.contains(substring) }

    // registerDebug can be used during dev to ensure they are actually being blocked.
    // use ` ChatBlocker.registerDebug("arbitraryLabel", { enabled }, Regex("^Profile ID: .+$"))
    // Remove the Debug part and the label when done testing
    fun registerDebug(label: String, enabled: () -> Boolean, pattern: Regex) =
        register(enabled) { s ->
            pattern.containsMatchIn(s).also {
                if (it) io.github.meridian.utils.modMessage("Blocked: $label")
            }
        }

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            val plain = message.string
            rules.none { it.enabled() && it.matches(plain) }
        }
    }
}
