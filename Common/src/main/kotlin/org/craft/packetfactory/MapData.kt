package org.craft.packetfactory

class MapData {
    var type: String = "PLAYER"
        get() = field.uppercase()

    var x: Byte = 0
    var z: Byte = 0

    var rotation: Byte = 0
        set(value) {
            if (field !in 0..15) {
                throw IllegalArgumentException("参数不正确,应该在0~15以内")
            }
            field = value
        }

    var name: String = ""
}