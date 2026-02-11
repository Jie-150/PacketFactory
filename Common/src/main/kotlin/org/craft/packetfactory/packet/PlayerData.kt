package org.craft.packetfactory.packet

import taboolib.common.platform.function.pluginId
import java.util.*

class PlayerData(val uuid: UUID, val entityId: Int) {
    var name = pluginId
}