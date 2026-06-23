package io.github.meridian.features.impl.general

import io.github.meridian.features.types.ButtonFeature
import net.minecraft.util.Util
import java.net.URI

object SoundListButton : ButtonFeature (
    name = "Sound List",
    description = "Takes you to a website that lists all sounds you can use for all of our sound replacement mods.\n§eOpens a new tab in your browser.",
    category = "General",
    configKey = "sound_list",
    subcategory = "Miscellaneous",
    buttonLabel = "Open Website",
    onClick = {
        Util.getPlatform().openUri(URI.create("https://www.digminecraft.com/lists/sound_list_pc.php"))
    },
)
