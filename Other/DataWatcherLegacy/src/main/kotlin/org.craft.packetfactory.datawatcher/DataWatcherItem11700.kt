package org.craft.packetfactory.datawatcher

import net.minecraft.core.BlockPosition
import net.minecraft.core.EnumDirection
import net.minecraft.core.Vector3f
import net.minecraft.core.particles.ParticleParam
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.syncher.DataWatcher
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.world.entity.EntityPose
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.IBlockData
import taboolib.module.nms.remap.require
import java.util.Optional
import java.util.OptionalInt
import java.util.UUID
import kotlin.jvm.java
import kotlin.jvm.optionals.getOrNull

internal class DataWatcherItem11700: DataWatcherItem {

    fun getDataWatcherItem(index: Int, value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcherRegistry.BYTE.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcherRegistry.INT.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcherRegistry.FLOAT.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcherRegistry.STRING.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcherRegistry.COMPONENT.createAccessor(index), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(index: Int, value: Optional<*>): DataWatcher.Item<out Optional<out Any?>?> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(DataWatcherRegistry.OPTIONAL_COMPONENT.createAccessor(index), value as Optional<IChatBaseComponent>)

            is BlockPosition -> DataWatcher.Item(DataWatcherRegistry.OPTIONAL_BLOCK_POS.createAccessor(index), value as Optional<BlockPosition>)

            is UUID -> DataWatcher.Item(DataWatcherRegistry.OPTIONAL_UUID.createAccessor(index), value as Optional<UUID>)

            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcherItem(index: Int, value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcherRegistry.ITEM_STACK.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Boolean): DataWatcher.Item<Boolean> {
        return DataWatcher.Item(DataWatcherRegistry.BOOLEAN.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcherRegistry.PARTICLE.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcherRegistry.ROTATIONS.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcherRegistry.BLOCK_POS.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcherRegistry.DIRECTION.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcherRegistry.COMPOUND_TAG.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcherRegistry.VILLAGER_DATA.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcherRegistry.OPTIONAL_UNSIGNED_INT.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcherRegistry.POSE.createAccessor(index), value)
    }

    fun getDataWatcherItem(index: Int, value: IBlockData): DataWatcher.Item<IBlockData> {
        return DataWatcher.Item(DataWatcherRegistry.BLOCK_STATE.createAccessor(index), value)
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
            is ItemStack -> getDataWatcherItem(index, toNMSItem(value))
            else -> error("不支持的类型: $value")
        }
    }


    private fun toNMSItem(itemStack: ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }
}