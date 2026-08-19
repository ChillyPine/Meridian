package io.github.meridian.features.impl.general

import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.gui.InventorySearch

object ItemSearchBar : SwitchFeature(
    name = "Item Search Bar",
    description = "Use && as a logical AND operator and lore: to search the lore instead of the name.",
    category = "General",
    configKey = "item_search_bar",
    subcategory = "Miscellaneous",
) {
    override fun onDeactivate() = InventorySearch.reset()
}
