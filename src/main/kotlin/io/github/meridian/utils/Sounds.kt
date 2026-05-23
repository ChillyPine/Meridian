package io.github.meridian.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

fun playClickSound(pitch: Float = 1.5f) {
    Minecraft.getInstance().soundManager.play(
        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch)
    )
}