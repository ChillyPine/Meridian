package io.github.meridian.utils

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.network.chat.Component
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The single permanent `ALLOW_GAME` veto listener. Features add/remove their [Rule]s reactively
 * (see [ChatBlockListener] + `SwitchFeature.blockChat*`), so a disabled blocker has no rule in the
 * list at all — the listener no longer polls a per-feature `enabled()` lambda on every message.
 */
object ChatBlocker {
    /**
     * Returns true to block (veto) the message. A rule may also perform a side effect when it
     * matches — e.g. re-add a cleaned copy of the message before vetoing the original.
     */
    fun interface Rule {
        fun shouldBlock(message: Component): Boolean
    }

    private val rules = CopyOnWriteArrayList<Rule>()

    fun add(rule: Rule) {
        rules.addIfAbsent(rule)
    }

    fun remove(rule: Rule) {
        rules.remove(rule)
    }

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
            rules.none { it.shouldBlock(message) }
        }
    }
}

/** A [Toggleable] that attaches/detaches a [ChatBlocker.Rule] based on its bound state. */
class ChatBlockListener(private val rule: ChatBlocker.Rule) : Toggleable() {
    override fun add() = ChatBlocker.add(rule)
    override fun remove() = ChatBlocker.remove(rule)
}
