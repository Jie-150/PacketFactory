package org.craft.packetfactory.data

import org.bukkit.attribute.AttributeModifier
import taboolib.library.xseries.XAttribute
import java.util.function.Consumer

class Attribute(var attribute: XAttribute) {

    var callback = Consumer<AttributeModifier> {}

    var base: Double = 0.0

    var operation = AttributeModifier.Operation.ADD_NUMBER
}