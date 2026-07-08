package org.craft.packetfactory.packet.dialog

class DialogButton(
    val label: String,
    val tooltip: String? = null,
    val width: Int = 150,
    val action: DialogClickAction? = null
)
