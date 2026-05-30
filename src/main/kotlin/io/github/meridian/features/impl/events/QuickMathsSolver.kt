package io.github.meridian.features.impl.events

import io.github.meridian.Meridian
import io.github.meridian.features.SwitchFeature
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.onChatMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object QuickMathsSolver : SwitchFeature(
    name = "QuickMaths",
    description = "Solves Math Teacher Primal Fear.",
    category = "Events",
    configKey = "quick_maths_solver",
    subcategory = "The Great Spook",
) {
    private const val PREFIX = "QUICK MATHS! Solve: "
    private val sanitize = Regex("[^-()\\d/*+.]")

    init {
        onChatMessage { text, _, _ ->
            if (!enabled) return@onChatMessage
            if (!text.startsWith(PREFIX)) return@onChatMessage
            val math = text.removePrefix(PREFIX)
            val calculation = math.replace("x", "*").replace(sanitize, "")
            val answer = runCatching { Parser(calculation).parse() }.getOrNull()
                ?: return@onChatMessage
            CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS).execute {
                Meridian.mc.execute {
                    modMessage("§dQuick Maths Answer: §a§l${format(answer)}")
                }
            }
        }
    }

    private fun format(d: Double): String =
        if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

    // Recursive-descent: + - * / and parentheses, decimal numbers.
    private class Parser(private val s: String) {
        private var i = 0
        fun parse(): Double = parseExpr().also { check(i == s.length) }
        private fun parseExpr(): Double {
            var v = parseTerm()
            while (i < s.length && (s[i] == '+' || s[i] == '-')) {
                val op = s[i++]
                val r = parseTerm()
                v = if (op == '+') v + r else v - r
            }
            return v
        }
        private fun parseTerm(): Double {
            var v = parseFactor()
            while (i < s.length && (s[i] == '*' || s[i] == '/')) {
                val op = s[i++]
                val r = parseFactor()
                v = if (op == '*') v * r else v / r
            }
            return v
        }
        private fun parseFactor(): Double {
            if (i < s.length && s[i] == '+') { i++; return parseFactor() }
            if (i < s.length && s[i] == '-') { i++; return -parseFactor() }
            if (i < s.length && s[i] == '(') {
                i++
                val v = parseExpr()
                check(i < s.length && s[i] == ')')
                i++
                return v
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            check(start != i)
            return s.substring(start, i).toDouble()
        }
    }
}