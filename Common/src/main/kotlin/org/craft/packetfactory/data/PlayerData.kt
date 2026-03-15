package org.craft.packetfactory.data

import org.bukkit.GameMode
import taboolib.common.platform.function.pluginId
import java.util.*

class PlayerData(val uuid: UUID, val entityId: Int) {

    var gamemode = GameMode.SURVIVAL

    var ping: Int = 0

    var name = pluginId

    var displayName: String? = null

    val properties: ArrayList<Property> = ArrayList()

    fun hasDisplayName() = displayName != null

    class Property(var name: String, var value: String, var signature: String?) {

        constructor(name: String, value: String) : this(name, value, null)

        fun hasSignature() = signature != null
    }

    enum class Type {
        ADD_PLAYER,UPDATE_GAME_MODE,UPDATE_LATENCY,UPDATE_DISPLAY_NAME,REMOVE_PLAYER
    }
}