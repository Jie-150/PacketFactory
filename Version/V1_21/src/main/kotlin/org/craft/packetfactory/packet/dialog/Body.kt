package org.craft.packetfactory.packet.dialog

import org.bukkit.inventory.ItemStack

sealed interface Body {

    data class Plain(val content: String, val width: Int = 200) : Body

    data class Item(
        val item: ItemStack,
        val description: Plain? = null,
        val showDecorations: Boolean = true,
        val showTooltip: Boolean = true,
        val width: Int = 68,
        val height: Int = 68
    ) : Body
}
