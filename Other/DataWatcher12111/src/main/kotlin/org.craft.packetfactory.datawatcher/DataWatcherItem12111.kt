package org.craft.packetfactory.datawatcher

import net.md_5.bungee.api.chat.BaseComponent
import net.minecraft.core.BlockPosition
import net.minecraft.core.Vector3f
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.syncher.DataWatcher
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.network.syncher.DataWatcherSerializer
import net.minecraft.world.entity.EntityLiving
import net.minecraft.world.entity.EntityPose
import net.minecraft.world.entity.EntityReference
import net.minecraft.world.item.ItemStack
import org.bukkit.Art
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_21_R7.CraftArt
import org.bukkit.craftbukkit.v1_21_R7.CraftParticle
import org.bukkit.craftbukkit.v1_21_R7.block.CraftBlock
import org.bukkit.craftbukkit.v1_21_R7.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftAxolotl
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftCat
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftChicken
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftCow
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftFrog
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPig
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftVillager
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftWolf
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_21_R7.util.CraftChatMessage
import org.bukkit.entity.Axolotl
import org.bukkit.entity.Cat
import org.bukkit.entity.Chicken
import org.bukkit.entity.Cow
import org.bukkit.entity.Frog
import org.bukkit.entity.Pig
import org.bukkit.entity.Pose
import org.bukkit.entity.Sniffer
import org.bukkit.entity.Villager
import org.bukkit.entity.Wolf
import org.bukkit.util.Vector
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.nms.MinecraftVersion
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.jvm.java

internal class DataWatcherItem12111 : DataWatcherItem {

    val version = MinecraftVersion.versionId

    fun Type.resolve(depth: Int = 1): Class<*> {
        val arg = (this as ParameterizedType).actualTypeArguments[0]
        return if (arg is ParameterizedType && depth != 1) {
            arg.resolve(if (depth == -1) -1 else depth - 1)
        } else (if (arg is ParameterizedType) arg.rawType else arg) as Class<*>
    }

    val data = buildMap {
        for (field in DataWatcherRegistry::class.java.declaredFields) {
            if (!Modifier.isStatic(field.modifiers)) continue
            val genericType = field.genericType
            if (genericType is ParameterizedType) {
                field.isAccessible = true
                val instance = field.get(null) ?: continue
                if (instance !is DataWatcherSerializer<*>) continue
                put(genericType.resolve(), instance)
            }
        }
    }

    private val types = HashMap<Class<*>, (Any) -> Any>()

    private fun registerItemStack() {
        types[org.bukkit.inventory.ItemStack::class.java] = { toNMSItem(it as org.bukkit.inventory.ItemStack) }
    }

    private fun registerPose() {
        types[Pose::class.java] = { EntityPose.valueOf((it as Pose).name) }
    }

    private fun registerBaseComponent() {
        types[BaseComponent::class.java] = { it as BaseComponent; CraftChatMessage.fromJSON(it.toPlainText()) }
    }

    private fun registerBlockFace() {
        types[BlockFace::class.java] = { CraftBlock.blockFaceToNotch(it as BlockFace) }
    }

    private fun registerLocation() {
        types[Location::class.java] = { it as Location; it.toPosition() }
    }

    private fun registerVector() {
        types[Vector::class.java] = { it as Vector; Vector3f(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
    }

    private fun registerParticle() {
        types[Particle::class.java] = { CraftParticle.bukkitToMinecraft(it as Particle) }
    }

    private fun registerOptional() {
        types[Optional::class.java] = { optionalHandler(it as Optional<*>) }
    }

    private fun registerCatVariant() {
        if (version < 11900) return
        types[Cat.Type::class.java] = { CraftCat.CraftType.bukkitToMinecraft(it as Cat.Type) }
    }

    private fun registerFrogVariant() {
        if (version < 11900) return
        types[Frog.Variant::class.java] = { CraftFrog.CraftVariant.bukkitToMinecraft(it as Frog.Variant) }
    }

    private fun registerPaintingVariant() {
        if (version < 11900) return
        types[Art::class.java] = { CraftArt.bukkitToMinecraft(it as Art) }
    }

    private fun registerSniffer() {
        types[Sniffer.State::class.java] = { net.minecraft.world.entity.animal.sniffer.Sniffer.State.valueOf((it as Sniffer.State).name) }
    }

    private fun registerList() {
        types[List::class.java] = { listHandler(it as List<*>) }
    }

    private fun registerWolfVariant() {
        if (version < 12005) return
        types[Wolf.Variant::class.java] = { CraftWolf.CraftVariant.bukkitToMinecraft(it as Wolf.Variant) }
    }

    private fun registerArmadillo() {
        if (version < 11700) return
        types[Axolotl.Variant::class.java] = { it as Axolotl.Variant; net.minecraft.world.entity.animal.axolotl.Axolotl.Variant.valueOf(it.name) }
    }

    private fun registerChickenVariant() {
        if (version < 12105) return
        types[Chicken.Variant::class.java] = { CraftChicken.CraftVariant.bukkitToMinecraft(it as Chicken.Variant) }
    }

    private fun registerCowVariant() {
        if (version < 12105) return
        types[Cow.Variant::class.java] = { CraftCow.CraftVariant.bukkitToMinecraft(it as Cow.Variant) }
    }

    private fun registerPigVariant() {
        if (version < 12105) return
        types[Pig.Variant::class.java] = { CraftPig.CraftVariant.bukkitToMinecraft(it as Pig.Variant) }
    }

    private fun registerVillagerType() {
        types[Villager.Type::class.java] = { CraftVillager.CraftType.bukkitToMinecraft(it as Villager.Type) }
    }

    private fun registerVillagerProfession() {
        types[Villager.Profession::class.java] = { CraftVillager.CraftProfession.bukkitToMinecraft(it as Villager.Profession) }
    }

    private fun registerVillager() {
        registerVillagerType()
        registerVillagerProfession()
    }

    private fun registerVariant() {
        registerArmadillo()
        registerPigVariant()
        registerCatVariant()
        registerCowVariant()
        registerFrogVariant()
        registerWolfVariant()
        registerChickenVariant()
        registerPaintingVariant()
    }

    private fun listHandler(list: List<*>): List<*> {
        return list.mapNotNull {
            it ?: return@mapNotNull null
            types[it.javaClass]?.invoke(it)
        }
    }

    private fun optionalHandler(obj: Optional<*>): Optional<*> {
        return Optional.of(
            when (val value = obj.get()) {
                is BaseComponent -> CraftChatMessage.fromJSON(value.toPlainText())
                is UUID -> EntityReference.of<EntityLiving>(value)

                is Location -> value.toPosition()
                is BlockData -> (value as CraftBlockData).state
                else -> error("Unexpected value: $value")
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDataWatcherItem(index: Int, value: Any): Any {
        if (value is DataWatcher.Item<*>) return value
        val type = types[value.javaClass]?.invoke(value) ?: value
        val accessor = data[type.javaClass]?.createAccessor(index) ?: error("不存在的类型: $type")
        return DataWatcher.Item::class.java.invokeConstructor(accessor, type)
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    private fun component(text: String): IChatBaseComponent {
        return CraftChatMessage.fromJSONOrString(text)
    }

    fun registerALL() {
        registerPose()
        registerList()
        registerVector()
        try {
            registerVariant()
        } catch (_: InvocationTargetException) {
        }
        registerSniffer()
        registerLocation()
        registerParticle()
        registerOptional()
        registerVillager()
        registerItemStack()
        registerBlockFace()
        registerBaseComponent()
    }

    fun Location.toPosition(): BlockPosition {
        return BlockPosition(blockX, blockY, blockZ)
    }

    init {
        registerALL()
    }
}