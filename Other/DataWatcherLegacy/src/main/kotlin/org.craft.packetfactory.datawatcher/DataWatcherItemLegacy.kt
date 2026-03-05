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
import org.craft.packetfactory.datawatcher.DataWatcherItem
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.remap.require
import java.util.Optional
import java.util.OptionalInt
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

internal class DataWatcherItemLegacy : DataWatcherItem {

    fun getDataWatcherItem(index: Int, value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcherRegistry.a.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcherRegistry.b.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcherRegistry.c.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcherRegistry.d.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcherRegistry.e.a(index), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(index: Int, value: Optional<*>): DataWatcher.Item<out Optional<out Any?>?> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(DataWatcherRegistry.f.a(index), value as Optional<IChatBaseComponent>)
            is IBlockData -> DataWatcher.Item(DataWatcherRegistry.h.a(index), value as Optional<IBlockData>)
            is BlockPosition -> DataWatcher.Item(DataWatcherRegistry.m.a(index), value as Optional<BlockPosition>)
            is UUID -> DataWatcher.Item(DataWatcherRegistry.o.a(index), value as Optional<UUID>)
            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcherItem(index: Int, value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcherRegistry.g.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Boolean): DataWatcher.Item<Boolean> {
        return if (MinecraftVersion.versionId >= 11605) {
            DataWatcher.Item(DataWatcherRegistry.i.a(index), value)
        } else {
            DataWatcher.Item(DataWatcherRegistry::class.java.getProperty<DataWatcherSerializer<Boolean>>("h", true)!!.a(index), value)
        }
    }

    fun getDataWatcherItem(index: Int, value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcherRegistry.j.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcherRegistry.k.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcherRegistry.l.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcherRegistry.n.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcherRegistry.p.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcherRegistry.q.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcherRegistry.r.a(index), value)
    }

    fun getDataWatcherItem(index: Int, value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcherRegistry.s.a(index), value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDataWatcherItem(index: Int, value: Any): Any {
        if (value is DataWatcher.Item<*>) return value
        if (require(EntityPose::class.java) && value is EntityPose) {
            return getDataWatcherItem(index, value)
        }
        if (require(VillagerData::class.java) && value is VillagerData) {
            return getDataWatcherItem(index, value)
        }
        if (require(ParticleParam::class.java) && value is ParticleParam) {
            return getDataWatcherItem(index, value)
        }
        return when (value) {
            is String -> getDataWatcherItem(index, value)
            is Byte -> getDataWatcherItem(index, value)
            is Float -> getDataWatcherItem(index, value)
            is NBTTagCompound -> getDataWatcherItem(index, value)
            is OptionalInt -> getDataWatcherItem(index, value)
            is Optional<*> -> getOptionalDataWatcher(index, value)
            is EnumDirection -> getDataWatcherItem(index, value)
            is BlockPosition -> getDataWatcherItem(index, value)
            is Vector3f -> getDataWatcherItem(index, value)
            is Boolean -> getDataWatcherItem(index, value)
            is IChatBaseComponent -> getDataWatcherItem(index, value)
            is ItemStack -> getDataWatcherItem(index, value)
            is Int -> getDataWatcherItem(index, value)
            is org.bukkit.inventory.ItemStack -> getDataWatcherItem(index, toNMSItem(value))
            else -> error("不支持的类型: $value")
        }
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

}