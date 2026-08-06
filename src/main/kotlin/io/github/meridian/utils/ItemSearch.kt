package io.github.meridian.utils

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

// Query grammar for the inventory search bar. `&&` separates terms and every
// term must match (logical AND); spaces inside a term are literal. A `lore:`
// marker switches the search target from the item name to the item lore and
// stays in effect for all following terms, across `&&`, to the end of the
// query — so `Hyperion lore:Sharpness && Wise` looks for "Wise" in the lore.
object ItemSearch {

    data class Term(val text: String, val lore: Boolean)

    private const val LORE_MARKER = "lore:"
    private val FORMATTING = Regex("§.")

    fun parse(query: String): List<Term> {
        val terms = mutableListOf<Term>()
        var lore = false
        for (chunk in query.split("&&")) {
            var rest = chunk
            while (true) {
                val marker = rest.indexOf(LORE_MARKER, ignoreCase = true)
                if (marker < 0) break
                addTerm(terms, rest.substring(0, marker), lore)
                lore = true
                rest = rest.substring(marker + LORE_MARKER.length)
            }
            addTerm(terms, rest, lore)
        }
        return terms
    }

    private fun addTerm(terms: MutableList<Term>, raw: String, lore: Boolean) {
        val text = raw.trim()
        if (text.isNotEmpty()) terms += Term(clean(text), lore)
    }

    fun matches(stack: ItemStack, terms: List<Term>): Boolean {
        if (terms.isEmpty()) return true
        if (stack.isEmpty) return false

        // Both are lazy so a name-only query never touches the lore component.
        val name = lazy { clean(stack.hoverName.string) }
        val lore = lazy {
            stack.get(DataComponents.LORE)?.lines()?.map { clean(it.string) } ?: emptyList()
        }

        return terms.all { term ->
            if (term.lore) lore.value.any { it.contains(term.text) }
            else name.value.contains(term.text)
        }
    }

    private fun clean(s: String) = FORMATTING.replace(s, "").lowercase()
}
