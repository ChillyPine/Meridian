package io.github.meridian.features.impl.general

import com.google.gson.JsonParser
import io.github.meridian.Meridian
import io.github.meridian.features.types.ButtonFeature
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.sendClientMessage
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.SemanticVersion
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.util.Util
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private const val RELEASES_API = "https://api.github.com/repos/ChillyPine/Meridian/releases/latest"

object UpdateChecker : SwitchFeature(
    name = "Check for Updates",
    description = "Checks GitHub for a newer Meridian release on join and notifies you in chat.",
    category = "General",
    configKey = "update_checker",
    subcategory = "Miscellaneous",
    defaultEnabled = true
) {
    // Fires the network call once per game session, on the first world join.
    @Volatile private var checked = false

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (checked || !enabled) return@register
            checked = true
            check()
        }
    }

    private fun check() {
        // GitHub rejects API requests without a User-Agent.
        val request = HttpRequest.newBuilder(URI.create(RELEASES_API))
            .header("User-Agent", "Meridian")
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build()

        HttpClient.newHttpClient()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept { response -> handle(response.body()) }
            .exceptionally {
                Meridian.logger.debug("Update check failed", it)
                null
            }
    }

    private fun handle(body: String) {
        val json = JsonParser.parseString(body).asJsonObject
        val tag = json.get("tag_name")?.asString ?: return
        val url = json.get("html_url")?.asString ?: return

        val latest = try {
            SemanticVersion.parse(tag.removePrefix("v"))
        } catch (e: Exception) {
            Meridian.logger.debug("Couldn't parse release tag '$tag'", e)
            return
        }

        val current = FabricLoader.getInstance()
            .getModContainer("meridian")
            .map { it.metadata.version }
            .orElse(null) ?: return

        if (current >= latest) return

        val border = "§5§m                                             "
        val clickStyle = Style.EMPTY
            .withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
            .withHoverEvent(HoverEvent.ShowText(Component.literal("§7Open the release page on GitHub")))

        val message = Component.literal("$border\n")
            .append("§6§l✦ §d§lMeridian Update Available §6§l✦\n")
            .append("  §7You're on §cv${current.friendlyString}§7, latest is §a$tag\n")
            .append(Component.literal("      §d§n» Click here to download «").setStyle(clickStyle))
            .append("\n$border")

        Meridian.mc.execute { sendClientMessage(message) }
    }
}

object LatestGHAction : ButtonFeature(
    name = "Download Latest Build",
    description = "Opens the GitHub actions page to download the latest build.",
    category = "General",
    configKey = "latest_gh_action",
    subcategory = "Miscellaneous",
    buttonLabel = "Open",
    onClick = {
        Util.getPlatform().openUri(URI.create("https://github.com/ChillyPine/Meridian/actions?query=branch%3Amain+is%3Asuccess"))
    },
)
