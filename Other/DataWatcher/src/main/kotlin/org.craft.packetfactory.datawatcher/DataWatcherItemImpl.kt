package org.craft.packetfactory.datawatcher

import net.minecraft.server.v1_16_R3.BlockPosition
import net.minecraft.server.v1_16_R3.DataWatcher
import net.minecraft.server.v1_16_R3.DataWatcherRegistry
import net.minecraft.server.v1_16_R3.DataWatcherSerializer
import net.minecraft.server.v1_16_R3.EntityPose
import net.minecraft.server.v1_16_R3.EnumDirection
import net.minecraft.server.v1_16_R3.IBlockData
import net.minecraft.server.v1_16_R3.IChatBaseComponent
import net.minecraft.server.v1_16_R3.ItemStack
import net.minecraft.server.v1_16_R3.NBTTagCompound
import net.minecraft.server.v1_16_R3.ParticleParam
import net.minecraft.server.v1_16_R3.Vector3f
import net.minecraft.server.v1_16_R3.VillagerData
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.remap.require
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.Optional
import java.util.OptionalInt
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

internal class DataWatcherItemImpl : DataWatcherItem {

    val data = buildMap {
        for (field in DataWatcherRegistry::class.java.declaredFields) {
            if (!Modifier.isStatic(field.modifiers)) continue
            val genericType = field.genericType
            if (genericType is ParameterizedType) {
                field.isAccessible = true
                val instance = field.get(null) ?: continue
                put(genericType.actualTypeArguments[0] as Class<*>, instance as net.minecraft.network.syncher.DataWatcherSerializer<*>)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDataWatcherItem(index: Int, value: Any): Any {
        if (value is DataWatcher.Item<*>) return value
        val nmsValue = if (value is org.bukkit.inventory.ItemStack) toNMSItem(value) else value
        val accessor = data[nmsValue.javaClass]?.createAccessor(index)
        return DataWatcher.Item::class.java.invokeConstructor(accessor, nmsValue)
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

}