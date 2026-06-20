package io.github.meridian.features.impl.general

import io.github.meridian.features.types.SwitchFeature
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.event.Event
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.PlainTextButton
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

object RemoveRealms : SwitchFeature(
    name = "Remove Realms Button",
    description = "Removes the realms button on the main menu.",
    category = "Vanilla",
    configKey = "remove_realms",
    subcategory = "Tweaks"
) {
    // Run our pass after everyone else's AFTER_INIT so vanilla (and any other
    // mod) has finished adding/repositioning the title-screen buttons first.
    private val REALMS_REMOVE_PHASE = Identifier.fromNamespaceAndPath("meridian", "realms_remove")

    init {
        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, REALMS_REMOVE_PHASE)

        ScreenEvents.AFTER_INIT.register(REALMS_REMOVE_PHASE) { _, screen, _, _ ->
            if (!enabled || screen !is TitleScreen) return@register

            val widgets = screen.children().filterIsInstance<AbstractWidget>()
            var realmsButton: Button? = null

            for (widget in widgets) {
                // Once the realms button is located, slide every button at or
                // below it up by its row height to close the gap it leaves.
                val realms = realmsButton
                if (realms != null && widget.y >= realms.y && widget !is PlainTextButton && widget.visible) {
                    widget.y -= 24
                }

                if (widget is Button && widget.message == Component.translatable("menu.online")) {
                    realmsButton = widget
                }
            }

            realmsButton?.let {
                it.visible = false
                it.active = false
            }
        }
    }
}