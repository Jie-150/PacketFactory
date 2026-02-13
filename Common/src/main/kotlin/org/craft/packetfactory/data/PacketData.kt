package org.craft.packetfactory.data

import com.google.common.base.Enums

@Suppress("UNCHECKED_CAST")
class PacketData {

    private val mapData = object : HashMap<String, Any>() {
        override fun put(key: String, value: Any): Any? {
            return super.put(key, value)
        }
    }

    fun write(key: String, value: Any) {
        if (contains(key)) {
            error("$key 已存在,不可重复写入")
        }
        mapData[key] = value
    }

    fun write(data: Map<String, Any>) {
        mapData.putAll(data.filter { mapData[it.key] == null })
    }

    fun <T> read(key: String): T {
        return (mapData[key] ?: error("$key 不可为空"))as T
    }

    fun readOrNull(key: String): Any? {
        return mapData[key]
    }

    fun <T> readOrNull(key: String): T? {
        return mapData[key] as T?
    }

    fun <T> readOrElse(key: String, default: T): T {
        return (mapData[key] ?: default) as T
    }

    fun <T> readNotNull(key: String, callback: (T) -> Unit): PacketData {
        if (contains(key)) {
            callback(mapData[key] as T)
        }
        return this
    }

    fun <T : Enum<T>> readEnum(clazz: Class<T>, key: String): T {
        return Enums.getIfPresent(clazz, mapData[key]?.toString()?.uppercase() ?: error("$key 不可为空")).get()
    }

    fun <T : Enum<T>> readEnumOrNull(clazz: Class<T>, key: String): T? {
        return Enums.getIfPresent(clazz, mapData[key]?.toString()?.uppercase() ?: return null).orNull()
    }

    fun <T : Enum<T>> readEnumOrElse(clazz: Class<T>, key: String, default: T): T {
        return Enums.getIfPresent(clazz, mapData[key]?.toString()?.uppercase() ?: return default).orNull() ?: default
    }

    operator fun contains(key: String): Boolean {
        return mapData.containsKey(key)
    }

    fun getSource(): Map<String, Any> {
        return HashMap(mapData)
    }

}