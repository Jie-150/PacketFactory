# PacketFactory

PacketFactory 是一个 Minecraft 服务器数据包封装库，提供跨版本的统一 API 来创建各种 NMS 网络数据包。

## 功能特性

- 支持 Minecraft 1.8 - 1.21 多版本
- 自动根据服务器版本选择对应实现
- 提供 200+ 种数据包创建方法
- 基于 TabooLib 框架构建

## 支持的数据包类型

- 实体相关：生成、销毁、元数据、装备、传送等
- 玩家相关：玩家信息更新、移除等
- 聊天消息：系统消息、操作栏、标题等
- 计分板：目标、分数、队伍等
- 世界效果：粒子、声音、爆炸等
- 窗口物品：物品栏、槽位等
- Boss 血条

## 使用方法

### 1. 添加依赖

在 `build.gradle.kts` 中添加：

```kotlin
repositories {
    maven("https://repo.xiao-jie.top/repository/maven-public/")
}

dependencies {
    taboo("org.craft:PacketFactory:版本号")
}
```

### 2. 配置 Relocate

为避免与其他插件冲突，需要重定向包路径：

```kotlin
taboolib {
    relocate("org.craft.packetfactory", "${project.group}.packetfactory")
}
```

### 3. 代码示例

```kotlin
// 获取 API 实例
val api = PacketFactory.getOutPacketAPI()

// 创建数据容器
val data = PacketData()
data.write("entityId", 12345)
data.write("uuid", UUID.randomUUID())
data.write("entityType", EntityType.ARMOR_STAND)
data.write("location", player.location)

// 创建数据包
val packet = api.createSpawnEntity(data)
```

## 核心 API

| 类 | 说明 |
|---|---|
| `PacketFactory` | 主入口，通过 `getOutPacketAPI()` 获取实例 |
| `NMSOut` | 核心接口，定义所有数据包创建方法 |
| `PacketData` | 数据容器，用于传递数据包参数 |

## 注意事项

- 库只负责创建数据包对象，发送需配合 TabooLib 或自己获取CraftPlayer的sendPacket方法
- 使用前确保 PacketData 包含所有必需参数
- 部分版本的某些方法可能未完全实现
