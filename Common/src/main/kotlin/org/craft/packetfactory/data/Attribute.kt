package org.craft.packetfactory.data

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import java.util.UUID
import java.util.function.Consumer

class Attribute(var attribute: Attribute) {

    var callback = Consumer<AttributeModifier>{}

    var base: Double = 0.0
}