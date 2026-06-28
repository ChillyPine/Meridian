package io.github.meridian.features

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.meridian.Meridian
import io.github.meridian.features.impl.dungeons.ShitterList
import io.github.meridian.hud.HudManager
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object FeatureManager {
    private const val CONFIG_VERSION = 1

    private val features = mutableListOf<Feature>()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val configDir: Path
        get() = FabricLoader.getInstance().configDir.resolve("meridian")
    private val configFile: Path
        get() = configDir.resolve("config.json")

    fun register(feature: Feature) {
        features += feature
    }

    fun all(): List<Feature> = features.toList()

    fun byCategory(category: String): List<Feature> =
        features.filter { it.category == category }

    fun save() {
        try {
            Files.createDirectories(configDir)
            val root = JsonObject()
            root.addProperty("version", CONFIG_VERSION)
            // Group by category -> subcategory -> configKey, derived from each
            // feature's own declared fields. New features need no extra code here:
            // they slot into the right place automatically from their category/
            // subcategory. Category and subcategory appear in first-registered order.
            val featuresJson = JsonObject()
            for (feat in features) {
                val featJson = JsonObject()
                feat.saveTo(featJson)
                if (featJson.size() == 0) continue
                val subName = feat.subcategory.ifBlank { "General" }
                val catObj = featuresJson.getAsJsonObject(feat.category)
                    ?: JsonObject().also { featuresJson.add(feat.category, it) }
                val subObj = catObj.getAsJsonObject(subName)
                    ?: JsonObject().also { catObj.add(subName, it) }
                subObj.add(feat.configKey, featJson)
            }
            root.add("features", featuresJson)
            val hudJson = JsonObject()
            HudManager.saveTo(hudJson)
            root.add("hud", hudJson)
            val shitterJson = JsonObject()
            ShitterList.saveTo(shitterJson)
            root.add("shitter", shitterJson)
            configFile.writeText(gson.toJson(root))
        } catch (e: Exception) {
            Meridian.logger.error("Failed to save Meridian config", e)
        }
    }

    // Walks the "features" node and loads each feature by configKey, wherever it
    // sits. Format-agnostic: handles the nested category/subcategory layout, the
    // older flat layout, and a feature that has since moved category. When a key
    // matches a feature we load it and stop descending (so we never misread a
    // feature's own nested state as a group), otherwise we recurse into the group.
    private fun loadFeaturesNode(node: JsonObject, byKey: Map<String, Feature>) {
        for ((key, value) in node.entrySet()) {
            if (!value.isJsonObject) continue
            val feat = byKey[key]
            if (feat != null) feat.loadFrom(value.asJsonObject)
            else loadFeaturesNode(value.asJsonObject, byKey)
        }
    }

    fun load() {
        try {
            if (!configFile.exists()) return
            val root = JsonParser.parseString(configFile.readText()).asJsonObject
            root.getAsJsonObject("features")?.let { loadFeaturesNode(it, features.associateBy { f -> f.configKey }) }
            root.getAsJsonObject("hud")?.let { HudManager.loadFrom(it) }
            root.getAsJsonObject("shitter")?.let { ShitterList.loadFrom(it) }
        } catch (e: Exception) {
            Meridian.logger.error("Failed to load Meridian config — falling back to defaults", e)
        }
    }
}
