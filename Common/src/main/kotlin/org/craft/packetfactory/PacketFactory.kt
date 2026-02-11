package org.craft.packetfactory

import org.craft.packetfactory.packet.NMSOut
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.nmsProxy

object PacketFactory {

    private val packet by lazy {
        val pack = buildString {
            append(NMSOut::class.java.`package`.name)
            append(".NMS")
            append(
                when (MinecraftVersion.major) {
                    9 -> "17"
                    10 -> "18"
                    11 -> "19"
                    12 -> "20"
                    13 -> "21"
                    else -> "OutImpl"
                }
            )
        }
        nmsProxy<NMSOut>(pack)
    }

    @JvmStatic
    fun getOutPacketAPI(): NMSOut {
        return packet
    }
}