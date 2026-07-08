package org.craft.packetfactory.datawatcher

import net.md_5.bungee.api.chat.BaseComponent
import net.minecraft.network.syncher.DataWatcherSerializer
import net.minecraft.server.v1_16_R3.*
import net.minecraft.world.entity.EntityLiving
import net.minecraft.world.entity.EntityReference
import org.bukkit.Art
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_16_R3.CraftArt
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftCat
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftChicken
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftCow
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftFrog
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPig
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftVillager
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftWolf
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
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

internal class DataWatcherItemImpl : DataWatcherItem {

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
        types[Particle::class.java] = { CraftParticle.toNMS(it as Particle) }
    }

    private fun registerOptional() {
        types[Optional::class.java] = { optionalHandler(it as Optional<*>) }
    }

    private fun registerCatVariant() {
        types[Cat.Type::class.java] = { CraftCat.CraftType.bukkitToMinecraft(it as Cat.Type) }
    }

    private fun registerFrogVariant() {
        types[Frog.Variant::class.java] = { CraftFrog.CraftVariant.bukkitToMinecraft(it as Frog.Variant) }
    }

    private fun registerPaintingVariant() {
        types[Art::class.java] = { CraftArt.BukkitToNotch(it as Art) }
    }

    private fun registerSniffer() {
        types[Sniffer.State::class.java] = { net.minecraft.world.entity.animal.sniffer.Sniffer.State.valueOf((it as Sniffer.State).name) }
    }

    private fun registerList() {
        types[List::class.java] = { listHandler(it as List<*>) }
    }

    private fun registerWolfVariant() {
        types[Wolf.Variant::class.java] = { CraftWolf.CraftVariant.bukkitToMinecraft(it as Wolf.Variant) }
    }

    private fun registerArmadillo() {
    }

    private fun registerChickenVariant() {
        types[Chicken.Variant::class.java] = { CraftChicken.CraftVariant.bukkitToMinecraft(it as Chicken.Variant) }
    }

    private fun registerCowVariant() {
        types[Cow.Variant::class.java] = { CraftCow.CraftVariant.bukkitToMinecraft(it as Cow.Variant) }
    }

    private fun registerPigVariant() {
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
                is UUID -> try {
                    DataWatcherRegistry.o
                    EntityReference.of<EntityLiving>(value)
                } catch (_: NoClassDefFoundError) {
                    value
                }

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
        registerItemStack()
        registerPose()
        registerBaseComponent()
        registerBlockFace()
        registerLocation()
        registerVector()
        registerParticle()
        registerOptional()
        registerVillager()
        registerVariant()
    }

    fun Location.toPosition(): BlockPosition {
        return BlockPosition(blockX, blockY, blockZ)
    }

    init {
        registerALL()
    }
}