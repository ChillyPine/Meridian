package io.github.meridian.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents

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
