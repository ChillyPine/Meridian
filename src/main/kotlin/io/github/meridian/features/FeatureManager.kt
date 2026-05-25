package io.github.meridian.features

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.meridian.Meridian
import io.github.meridian.utils.ESP
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
            .sortedBy { it.subcategory }

    fun save() {
        try {
            Files.createDirectories(configDir)
            val root = JsonObject()
            root.addProperty("version", CONFIG_VERSION)
            val featuresJson = JsonObject()
            for (feat in features) {
                val featJson = JsonObject()
                feat.saveTo(featJson)
                if (featJson.size() > 0) featuresJson.add(feat.configKey, featJson)
            }
            root.add("features", featuresJson)
            val espJson = JsonObject()
            ESP.saveTo(espJson)
            root.add("esp", espJson)
            configFile.writeText(gson.toJson(root))
        } catch (e: Exception) {
            Meridian.logger.error("Failed to save Meridian config", e)
        }
    }

    fun load() {
        try {
            if (!configFile.exists()) return
            val root = JsonParser.parseString(configFile.readText()).asJsonObject
            val featuresJson = root.getAsJsonObject("features")
            if (featuresJson != null) {
                for (feat in features) {
                    val featJson = featuresJson.getAsJsonObject(feat.configKey) ?: continue
                    feat.loadFrom(featJson)
                }
            }
            root.getAsJsonObject("esp")?.let { ESP.loadFrom(it) }
        } catch (e: Exception) {
            Meridian.logger.error("Failed to load Meridian config — falling back to defaults", e)
        }
    }
}
