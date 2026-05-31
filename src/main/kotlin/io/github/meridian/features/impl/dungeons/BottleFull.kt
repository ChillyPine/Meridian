package io.github.meridian.features.impl.dungeons

// Notifies of storm bottle full (and variants)
//object BottleFull (
//)

// ---------------------------------------------------------------------------
// Kotlin equivalent of the CT ItemUtils.InvCheck(name).
//
// CT:
//   Player.getInventory().getItems().find(a => a?.getName()?.includes(name))
//
// Mojang-mappings notes (verified against the 1.21.11 jar):
//   - Player.getInventory()          -> net.minecraft.world.entity.player.Inventory
//   - Inventory.getNonEquipmentItems() -> NonNullList<ItemStack>, the 36 main
//       slots (hotbar 0-8 + main inventory 9-35). The old `inventory.items`
//       field is gone after the 1.21.2 inventory refactor — use this instead.
//   - ItemStack.getHoverName()       -> Component (display name, custom-name
//       aware). CT's getName(). Read the plain text with `.string`.
//   - ItemStack.isEmpty()            -> skip air slots.
//
//   import io.github.meridian.Meridian
//
//   // Returns the first matching stack (mirrors CT's `find`), or null.
//   fun invCheck(name: String): ItemStack? {
//       val player = Meridian.mc.player ?: return null
//       return player.inventory.nonEquipmentItems.firstOrNull { stack ->
//           !stack.isEmpty && stack.hoverName.string.contains(name)
//       }
//   }
//
//   // Boolean-only variant, if you just need "is it in there".
//   fun hasItem(name: String): Boolean {
//       val player = Meridian.mc.player ?: return false
//       return player.inventory.nonEquipmentItems.any { stack ->
//           !stack.isEmpty && stack.hoverName.string.contains(name)
//       }
//   }
//
//   // To read every item name (e.g. for debugging):
//   //   player.inventory.nonEquipmentItems
//   //       .filterNot { it.isEmpty }
//   //       .map { it.hoverName.string }
//
// Note: getNonEquipmentItems() excludes armor and offhand (same as CT's
// getItems()). If the bottle can sit in the offhand too, also check
// `player.offhandItem`.
// ---------------------------------------------------------------------------