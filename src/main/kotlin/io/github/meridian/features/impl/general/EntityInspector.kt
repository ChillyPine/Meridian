package io.github.meridian.features.impl.general

import io.github.meridian.Meridian.mc
import io.github.meridian.features.types.SwitchFeature
import io.github.meridian.utils.Keybinds
import io.github.meridian.utils.getChatBreak
import io.github.meridian.utils.modMessage
import io.github.meridian.utils.sendClientMessage
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import java.util.Base64
import java.util.Locale

object EntityInspector : SwitchFeature(
    name = "Entity Inspector",
    description = "Press the keybind to print info about the entity you're looking at into chat.\nSet Keybind in Minecraft controls.",
    category = "General",
    configKey = "entity_inspector",
    subcategory = "Developer",
) {
    private val SLOTS = listOf(
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
        EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
    )
    private val TEXTURE_HASH = Regex("texture/([0-9a-fA-F]+)")

    init {
        onTick {
            val key = Keybinds.devInspectEntity ?: return@onTick
            var pressed = false
            while (key.consumeClick()) pressed = true
            if (pressed) inspect()
        }
    }

    private fun inspect() {
        val target = pickEntity()
        if (target == null) {
            modMessage("§cNo entity under your crosshair.", delayMs = 0)
            return
        }
        // Every line also goes into the plain-text copy so [Copy All] matches what's on screen.
        val plain = StringBuilder()
        val out = mutableListOf<Component>()
        fun line(c: Component) { out += c; plain.appendLine(c.string) }

        build(target, ::line)

        val copyAll = clickable("§a[Copy All]", plain.toString().trimEnd(), "Copy this whole dump")
        out.add(1, Component.literal("§6§lEntity Inspector ").append(copyAll))

        sendClientMessage(getChatBreak(), 0)
        out.forEach { sendClientMessage(it, 0) }
        sendClientMessage(getChatBreak(), 0)
    }

    /** Entity under the crosshair, out to render distance but never past the first block hit. */
    fun pickEntity(): Entity? {
        val player = mc.player ?: return null
        val range = (mc.options.effectiveRenderDistance + 1) * 16.0
        val eye = player.getEyePosition(1f)
        val look = player.getViewVector(1f)

        val blockHit = player.pick(range, 1f, false)
        val maxDist = eye.distanceTo(blockHit.location)
        val end = eye.add(look.scale(maxDist))
        val box = player.boundingBox.expandTowards(look.scale(maxDist)).inflate(1.0)

        return ProjectileUtil.getEntityHitResult(
            player, eye, end, box, { it !== player && !it.isSpectator }, maxDist * maxDist,
        )?.entity
    }

    private fun build(e: Entity, line: (Component) -> Unit) {
        val player = mc.player ?: return
        val typeId = BuiltInRegistries.ENTITY_TYPE.getKey(e.type).toString()

        line(Component.literal("§f${e.type.description.string} §8#${e.id}"))
        line(field("Type", clickable("§f$typeId", typeId)))
        line(field("UUID", clickable("§f${e.stringUUID}", e.stringUUID).append(" §8(v${e.uuid.version()})")))
        line(field("Name", e.customName?.let { nameComponent(it) } ?: Component.literal("§8None")))
        line(field("Pos", Component.literal(
            "§f${fmt(e.x)}, ${fmt(e.y)}, ${fmt(e.z)} §7[${fmt(player.distanceTo(e).toDouble(), 1)} blocks]"
        )))
        line(field("Size", Component.literal("§f${fmt(e.bbWidth.toDouble())} x ${fmt(e.bbHeight.toDouble())}")))

        if (e is LivingEntity) {
            line(field("Health", Component.literal("§f${fmt(e.health.toDouble(), 1)} / ${fmt(e.maxHealth.toDouble(), 1)}")))
            if (e.armorValue > 0) line(field("Armor", Component.literal("§f${e.armorValue}")))
        }

        line(field("Flags", Component.literal(flags(e).ifEmpty { listOf("§8None") }.joinToString("§7, ") { "§f$it" })))
        e.team?.let { line(field("Team", clickable("§f${it.name}", it.name))) }

        if (e is Player) {
            val profile = e.gameProfile
            line(field("Profile", Component.literal("§f${profile.name()}")))
            profile.properties()["textures"].firstOrNull()?.value()?.let { line(field("Skin", texture(it))) }
        }

        e.vehicle?.let { line(field("Riding", Component.literal(shortRef(it, e)))) }
        if (e.passengers.isNotEmpty()) {
            line(field("Passengers", Component.literal(e.passengers.joinToString("§7, ") { shortRef(it, e) })))
        }

        if (e is LivingEntity) {
            val gear = SLOTS.map { it to e.getItemBySlot(it) }.filter { !it.second.isEmpty }
            if (gear.isEmpty()) {
                line(field("Equipment", Component.literal("§8None")))
            } else {
                line(header("Equipment"))
                gear.forEach { (slot, stack) -> equipmentLines(slot, stack, line) }
            }

            if (e.activeEffects.isNotEmpty()) {
                line(header("Effects"))
                e.activeEffects.forEach {
                    val name = Component.translatable(it.descriptionId).string
                    val secs = if (it.isInfiniteDuration) "∞" else "${it.duration / 20}s"
                    line(Component.literal("  §f$name ${it.amplifier + 1} §8($secs)"))
                }
            }
        }

        //attempts to also find the skyblock name on the armor stand
        val stands = e.level().getEntitiesOfClass(ArmorStand::class.java, e.boundingBox.inflate(1.0, 3.0, 1.0))
            .filter { it !== e && it.customName != null }
            .sortedByDescending { it.y }
        if (stands.isNotEmpty()) {
            line(header("Nearby Name Stands"))
            stands.forEach {
                val off = it.id - e.id
                val dy = it.y - e.y
                line(
                    Component.literal("  §8#${it.id} §7(${if (off >= 0) "+" else ""}$off) §7Δy ${fmt(dy, 2)} ")
                        .append(nameComponent(it.customName!!))
                )
            }
        }
    }

    private fun equipmentLines(slot: EquipmentSlot, stack: ItemStack, line: (Component) -> Unit) {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item).toString()
        val row = Component.literal("  §7${slot.getName().replaceFirstChar { it.uppercase() }}: ")
            .append(stack.hoverName.copy())
            .append(" ")
            .append(clickable("§8($itemId)", itemId))
        if (stack.count > 1) row.append(" §7x${stack.count}")
        line(row)

        stack.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.orElse(null)?.let {
            line(Component.literal("    §7SB ID: ").append(clickable("§e$it", it)))
        }
        stack.get(DataComponents.PROFILE)?.partialProfile()?.properties()?.get("textures")
            ?.firstOrNull()?.value()?.let { line(Component.literal("    §7Texture: ").append(texture(it))) }
    }

    private fun flags(e: Entity): List<String> = buildList {
        if (e.isInvisible) add("Invisible")
        if (e.isCurrentlyGlowing) add("Glowing")
        if (e.isOnFire) add("On Fire")
        if (e.isNoGravity) add("No Gravity")
        if (e.isCustomNameVisible) add("Name Visible")
        if (e is ArmorStand && e.isMarker) add("Marker")
    }

    // custom skin texture graber
    private fun texture(base64: String): Component {
        val hash = runCatching { String(Base64.getDecoder().decode(base64)) }.getOrNull()
            ?.let { TEXTURE_HASH.find(it)?.groupValues?.get(1) }
        val shown = hash?.let { "§b${it.take(16)}…" } ?: "§b${base64.take(16)}…"
        return clickable(shown, base64, "Click to copy the full texture value")
    }

    private fun nameComponent(name: Component): MutableComponent =
        name.copy().withStyle { s ->
            s.withHoverEvent(HoverEvent.ShowText(Component.literal("§7Click to copy plain text")))
                .withClickEvent(ClickEvent.CopyToClipboard(name.string))
        }

    private fun shortRef(other: Entity, from: Entity): String {
        val off = other.id - from.id
        return "§f${other.type.description.string} §8#${other.id} (${if (off >= 0) "+" else ""}$off)"
    }

    private fun clickable(text: String, copy: String, hover: String = "Click to copy"): MutableComponent =
        Component.literal(text).withStyle { s ->
            s.withHoverEvent(HoverEvent.ShowText(Component.literal("§7$hover")))
                .withClickEvent(ClickEvent.CopyToClipboard(copy))
        }

    private fun field(label: String, value: Component): Component =
        Component.literal("§7$label: ").append(value)

    private fun header(label: String): Component = Component.literal("§6$label")

    private fun fmt(v: Double, places: Int = 2): String = String.format(Locale.ROOT, "%.${places}f", v)
}
