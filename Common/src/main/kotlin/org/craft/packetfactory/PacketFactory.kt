package org.craft.packetfactory

import org.craft.packetfactory.datawatcher.DataWatcherItem
import org.craft.packetfactory.packet.NMSPacket
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy

object PacketFactory {
    private val packet by lazy {
        val version = when (MinecraftVersion.major) {
            9 -> "17"
            10 -> "18"
            11 -> "19"
            12 -> "20"
            13 -> "21"
            else -> "Legacy"
        }
        nmsProxy<NMSPacket>("{name}$version")
    }

    private val dataPacket by lazy {
        nmsProxy<DataWatcherItem>("{name}${if (MinecraftVersion.versionId >= 12111) 12111 else "Impl"}")
    }

    @JvmStatic
    fun getOutPacketAPI(): NMSPacket {
        return packet
    }

    @JvmStatic
    fun getDataWatcherItemAPI(): DataWatcherItem {
        return dataPacket
    }
}