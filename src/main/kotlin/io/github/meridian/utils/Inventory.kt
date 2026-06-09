package io.github.meridian.utils

import io.github.meridian.Meridian
import net.minecraft.world.item.ItemStack

// should return the first matching stack name and its amount? prob
fun invCheck(name: String): ItemStack? {
    val player = Meridian.mc.player ?: return null
    val inv = player.inventory
    return (0 until inv.containerSize).map { inv.getItem(it) }.firstOrNull { stack ->
        !stack.isEmpty && stack.hoverName.string.contains(name)
    }
}

// returns true if item is in the inventory
fun hasItem(name: String): Boolean {
    val player = Meridian.mc.player ?: return false
    val inv = player.inventory
    return (0 until inv.containerSize).any { i ->
        val stack = inv.getItem(i)
        !stack.isEmpty && stack.hoverName.string.contains(name)
    }
}