package org.craft.packetfactory.data

import org.bukkit.GameMode
import taboolib.common.platform.function.pluginId
import java.util.*

class PlayerData(val uuid: UUID, val entityId: Int) {

    var gamemode  = GameMode.SURVIVAL

    var ping : Int = 0

    var name = pluginId
}