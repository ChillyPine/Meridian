package io.github.meridian.features.impl.general

import io.github.meridian.features.types.ButtonFeature
import com.mojang.blaze3d.Blaze3D
import java.net.URI

object SoundListButton : ButtonFeature (
    name = "Sound List",
    description = "Takes you to a website that lists all sounds you can use for all sound replacement mods.\n§eOpens a new tab in your browser.",
    category = "General",
    configKey = "sound_list",
    subcategory = "Miscellaneous",
    buttonLabel = "Open Website",
    onClick = {
        Blaze3D.openUri(URI.create("https://www.digminecraft.com/lists/sound_list_pc.php"))
    },
)
