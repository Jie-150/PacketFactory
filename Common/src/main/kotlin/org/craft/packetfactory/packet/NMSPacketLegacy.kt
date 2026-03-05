package org.craft.packetfactory.packet

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.tree.RootCommandNode
import net.minecraft.server.v1_12_R1.ChatComponentText
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.command.TabCompleter
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSets
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle
import org.bukkit.craftbukkit.v1_16_R3.CraftSound
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeMap
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import org.craft.packetfactory.PacketFactory
import org.craft.packetfactory.data.MapData
import org.craft.packetfactory.data.PacketData
import org.craft.packetfactory.data.PlayerData
import taboolib.common.platform.function.pluginId
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.createDataSerializer
import taboolib.module.nms.remap.require
import taboolib.module.nms.saveToString
import java.util.*

internal class NMSPacketLegacy : NMSPacket {

    override fun createSpawnEntity(data: PacketData): Any {
        val entityType = data.read<EntityType>("entityType")
        val entityTypeId = entityType.typeId.toByte()
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val location = data.readOrElse("location", emptyLocation)
        val yaw = mathRot(fixYaw(entityType, location.yaw))
        val dataValue = data.readOrElse("data", 0)
        return NMS16SpawnEntity().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeUUID(uuid)
                writeByte(entityTypeId)
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeFloat(mathRot(location.pitch))
                writeFloat(yaw)
                writeInt(dataValue)
                writeShort(0)
                writeShort(0)
                writeShort(0)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createSpawnEntityLiving(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val entityType = data.readOrElse("entityType", EntityType.PLAYER)
        val location = data.readOrElse("location", emptyLocation)
        val yaw = mathRot(fixYaw(entityType, location.yaw))
        val extraData = data.readOrElse("data", 0)

        return NMS16SpawnEntityLiving().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeUUID(uuid)
                writeInt(entityType.typeId.toInt())
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeByte(mathRot(location.pitch).toInt().toByte())
                writeByte(yaw.toInt().toByte())
                writeByte(extraData.toByte())
                writeShort(0)
                writeShort(0)
                writeShort(0)
                if (MinecraftVersion.versionId < 11600) {
                    it.setProperty("n", listOf(DataWatcher(null)))
                }
            }.build() as PacketDataSerializer)
        }

    }

    override fun createSpawnEntityExperienceOrb(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val count = data.readOrElse("count", 0)
        return NMS16SpawnEntityExperienceOrb().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeShort(count.toShort())
            }.build() as PacketDataSerializer)
        }
    }

    override fun createEntityTeleport(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        val onGround = data.readOrElse("onGround", false)
        return NMS16EntityTeleport().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeFloat(mathRot(location.pitch))
                writeFloat(mathRot(location.yaw))
                writeBoolean(onGround)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createEntityHeadRotation(data: PacketData): Any {
        val entityId = data.readOrElse("entityId", 0)
        return NMS16EntityHeadRotation().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeFloat(mathRot(data.read("yHeadRot")))
            }.build() as PacketDataSerializer)
        }
    }

    override fun createEntityMetadata(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val items = data.readOrElse<Map<Int, Any>>("items", mapOf()).map {
            PacketFactory.getDataWatcherItemAPI().getDataWatcherItem(it.key, it) as DataWatcher.Item<*>
        }
        return NMS16EntityMetadata().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeMetadataLegacy(items)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createAnimation(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val animation = data.read<Int>("animation")
        return NMS16Animation().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeByte(animation.toByte())
            }.build() as PacketDataSerializer)
        }
    }

    override fun createAttachEntity(data: PacketData): Any {
        val attackId = data.read<Int>("attackId")
        val entityId = data.read<Int>("entityId")
        return NMS16AttachEntity().also {
            it.a(createDataSerializer {
                writeInt(attackId)
                writeInt(entityId)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createBed(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        return NMS12Bed().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeBlockPosition(location.blockX, location.blockY, location.blockZ)
            }.build() as net.minecraft.server.v1_12_R1.PacketDataSerializer)
        }
    }

    override fun createKeepAlive(data: PacketData): Any {
        val keepAliveID = data.read<Long>("keepAliveID")
        return NMS16KeepAlive(keepAliveID)
    }

    /** 暂未实现 */
    override fun createCustomPayload(data: PacketData): Any {
        return NMS16CustomPayload()
    }

    override fun createPlayerChat(data: PacketData): Any {
        val text = component(data.read("text"))
        val type = data.readEnumOrElse(ChatMessageType::class.java, "type", ChatMessageType.SYSTEM)
        val uuid = data.read<UUID>("uuid")
        return NMS16Chat(text, type, uuid)
    }

    override fun createPlayerCombatEnd(data: PacketData): Any {
        val entity = data.readOrElse("entity", -1)
        val duration = data.readOrElse("duration", 0)
        return NMS16CombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.END_COMBAT)
            it.setProperty("d", duration)
            it.setProperty("c", entity)
        }
    }

    override fun createPlayerCombatEnter(data: PacketData): Any {
        return NMS16CombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.ENTER_COMBAT)
        }
    }

    override fun createPlayerCombatKill(data: PacketData): Any {
        val attack = data.read<Int>("entityId")
        val entity = data.readOrElse("entity", -1)
        val text = component(data.read("text"))
        return NMS16CombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED)
            it.setProperty("b", attack)
            it.setProperty("c", entity)
            it.setProperty("e", text)
        }
    }

    override fun createPlayerInfoRemove(data: PacketData): Any {
        val players = data.read<List<GameProfile>>("players")
        return NMS16PlayerInfo().also {
            it.setProperty("a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER)
            val b = players.map { p ->
                it.PlayerInfoData(p, 0, EnumGamemode.SURVIVAL, null)
            }
            it.setProperty("b", b)

        }
    }

    override fun createPlayerInfoUpdate(data: PacketData): Any {
        val type = data.readEnum(
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction::class.java,
            "type",
        )

        return NMS16PlayerInfo().also {
            it.setProperty("a", type)
            val b = data.read<List<PlayerData>>("players").map { p ->
                it.PlayerInfoData(GameProfile(p.uuid, p.name), p.ping, EnumGamemode.SURVIVAL, null)
            }
            it.setProperty("b", b)
        }
    }

    override fun createSetActionBarText(data: PacketData): Any {
        val text = component(data.read("text"))
        val uuid = data.read<UUID>("uuid")
        return NMS16Chat(text, ChatMessageType.GAME_INFO, uuid)
    }

    override fun createSetBorderSize(data: PacketData): Any {
        val action = data.readEnum(PacketPlayOutWorldBorder.EnumWorldBorderAction::class.java, "action")
        val scale = data.readOrElse("scale", 1.0)
        val x = data.read<Double>("x")
        val z = data.read<Double>("z")
        val size = data.read<Double>("size")
        val distance = data.readOrElse("distance", 10)
        val time = data.readOrElse("time", 0)
        return NMS16WorldBorder().also {
            it.setProperty("a", action)
            it.setProperty("c", x * scale)
            it.setProperty("d", z * scale)
            // 这三个方法还不懂作用
            // e -> LerpTarget
            it.setProperty("e", 0)
            // g -> LerpTime
            it.setProperty("g", 0)
            // b -> getAbsoluteMaxSize
            it.setProperty("b", 0)
            it.setProperty("f", size)
            it.setProperty("i", distance)
            it.setProperty("h", time)
        }
    }

    override fun createSystemChat(data: PacketData): Any {
        val text = component(data.read("text"))
        val uuid = data.read<UUID>("uuid")
        return NMS16Chat(text, ChatMessageType.SYSTEM, uuid)
    }

    override fun createAbilities(data: PacketData): Any {
        val abilities = data.bind(PlayerAbilities())
            .readNotNull<Boolean>("isFlying") {
                isFlying = it
            }.readNotNull<Boolean>("canFly") {
                canFly = it
            }.readNotNull<Boolean>("isInvulnerable") {
                isInvulnerable = it
            }.readNotNull<Boolean>("mayBuild") {
                mayBuild = it
            }.get()
        return NMS16Abilities(abilities)
    }

    /** 暂未实现 */
    override fun createAdvancements(data: PacketData): Any {
        return NMS16Advancements()
    }

    /** 暂未实现 */
    override fun createAutoRecipe(data: PacketData): Any {
        val id = data.read<Int>("id")
        return NMS16AutoRecipe()
    }

    override fun createBlockAction(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val block = IRegistry.BLOCK[MinecraftKey(data.read<String>("block"))]
        val action = data.readOrElse("action", 0)
        val param = data.readOrElse("param", 0)
        return NMS16BlockAction(location, block, action, param)
    }

    override fun createBlockBreakAnimation(data: PacketData): Any {
        val id = data.read<Int>("id")
        val location = data.read<Location>("location").toPosition()
        val progress = data.readOrElse("progress", 0)
        return NMS16BlockBreakAnimation(id, location, progress)
    }

    override fun createBlockChange(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val block = (Blocks::class.java.getField(data.read<String>("block")).get(null) as Block).blockData
        return NMS16BlockChange(location, block)
    }

    override fun createBossBar(data: PacketData): Any {
        val action = data.readEnum(PacketPlayOutBoss.Action::class.java, "action")
        val text = component(data.read("text"))
        val boss = BossBattleCustom(MinecraftKey(pluginId + "_custom_bossbar"), text)
        boss.a(data.read<UUID>("uuid"))
        boss.a(data.readEnumOrElse(BossBattle.BarColor::class.java, "color", BossBattle.BarColor.WHITE))
        boss.a(data.readOrElse("progress", 0.0f))
        boss.a(data.readEnumOrElse(BossBattle.BarStyle::class.java, "style", BossBattle.BarStyle.PROGRESS))
        boss.a(data.readOrElse("isDarkenSky", false))
        boss.b(data.readOrElse("isPlayMusic", false))
        boss.c(data.readOrElse("isCreateFog", false))
        return NMS16Boss(action, boss)
    }

    override fun createCamera(data: PacketData): Any {
        return NMS16Camera().also {
            it.a(createDataSerializer {
                writeInt(data.read("cameraId"))
            }.build() as PacketDataSerializer)
        }
    }

    /** 咱不实现 */
    override fun createCommands(data: PacketData): Any {
        val a: RootCommandNode<ICompletionProvider>
        return NMS16Commands()
    }

    override fun createEntityEffect(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val effectId = data.read<Int>("effect")
        val duration = data.readOrElse("duration", 0)
        val amplification = data.readOrElse("amplification", 0)
        val ambient = data.readOrElse("ambient", false)
        val showParticles = data.readOrElse("showParticles", false)
        val showIcon = data.readOrElse("showIcon", false)
        val mobEffect = MobEffect(MobEffectList.fromId(effectId), duration, amplification, ambient, showParticles, showIcon, null)
        return NMS16EntityEffect(entityId, mobEffect)
    }

    override fun createEntitySound(data: PacketData): Any {
        val soundEffect = CraftSound.getSoundEffect(data.readEnum(Sound::class.java, "sound"))
        val soundCategory = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val entityId = data.read<Int>("entityId")
        val volume = data.readOrElse("volume", 1.0f)
        val pitch = data.readOrElse("pitch", 1.0f)
        return NMS16EntitySound().also {
            it.a(createDataSerializer {
                writeInt(IRegistry.SOUND_EVENT.a(soundEffect))
                writeByte(soundCategory.ordinal.toByte())
                writeInt(entityId)
                writeFloat(volume)
                writeFloat(pitch)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createEntityStatus(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val status = data.read<Byte>("status")
        return NMS16EntityStatus().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeByte(status)
            }.build() as PacketDataSerializer)
        }
    }

    override fun createExplosion(data: PacketData): Any {
        val location = data.read<Location>("location")
        val strength = data.readOrElse("strength", 1.0f)
        val locations = data.readOrElse("locations", emptyList<Location>()).map { it.toPosition() }
        val motion = data.readOrElse("motion", Vector())
        return NMS16Explosion(
            location.x,
            location.y,
            location.z,
            strength,
            locations,
            Vec3D(motion.x, motion.y, motion.z)
        )
    }

    override fun createGameStateChange(data: PacketData): Any {
        val change = PacketPlayOutGameStateChange.a(data.read("state"))
        val param = data.readOrElse("param", 0.0f)
        return NMS16GameStateChange(change, param)
    }

    override fun createLightUpdate(data: PacketData): Any {
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        val trustEdges = data.readOrElse("trustEdges", false)
        val skyYMask = data.read<Int>("skyYMask")
        val blockYMask = data.read<Int>("blockYMask")
        val emptySkyYMask = data.readOrElse("emptySkyYMask", 0)
        val emptyBlockYMask = data.readOrElse("emptyBlockYMask", 0)
        val skyUpdates = data.readOrElse<List<ByteArray>>("skyUpdates", listOf())
        val blockUpdates = data.readOrElse<List<ByteArray>>("blockUpdates", listOf())
        return NMS16LightUpdate().also {
            it.a(createDataSerializer {
                writeVarInt(x)
                writeVarInt(z)
                writeBoolean(trustEdges)
                writeVarInt(skyYMask)
                writeVarInt(blockYMask)
                writeVarInt(emptySkyYMask)
                writeVarInt(emptyBlockYMask)
                skyUpdates.forEachIndexed { index, bytes ->
                    if (index >= skyYMask){
                        return@forEachIndexed
                    }
                    writeVarInt(2048)
                    writeBytes(bytes)
                }
                blockUpdates.forEachIndexed { index, bytes ->
                    if (index >= blockYMask){
                        return@forEachIndexed
                    }
                    writeVarInt(2048)
                    writeBytes(bytes)
                }
            }.build() as PacketDataSerializer)
        }
    }

    override fun createLogin(data: PacketData): Any {
        TODO("该包目前不知道什么作用,参数过多暂时不做")
    }

    override fun createLookAt(data: PacketData): Any {
        val anchor = data.readEnumOrElse(ArgumentAnchor.Anchor::class.java, "anchor", ArgumentAnchor.Anchor.EYES)
        val location = data.read<Location>("location")
        return NMS16LookAt().also {
            it.setProperty("e", anchor)
            it.setProperty("a", location.x)
            it.setProperty("b", location.y)
            it.setProperty("c", location.z)
        }
    }

    override fun createMap(data: PacketData): Any {
        val mapId = data.read<Int>("mapId")
        val scale = data.readOrElse("scale", 1.toByte())
        val track = data.readOrElse("track", false)
        val locked = data.readOrElse("locked", false)
        val colors = data.readOrElse("colors", ByteArray(128 * 128))
        val maps = data.readOrElse("map", emptyList<MapData>()).map {
            MapIcon(MapIcon.Type.valueOf(it.type), it.x, it.z, it.rotation, component(it.name))
        }
        val startX = data.readOrElse("startX", 0)
        val startZ = data.readOrElse("startZ", 0)
        val width = data.readOrElse("width", 0)
        val height = data.readOrElse("height", 0)
        return NMS16Map(mapId, scale, track, locked, maps, colors, startX, startZ, width, height)
    }

    override fun createMount(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val passengers = data.readOrElse("passengers", emptyList<Int>())

        return NMS16Mount().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeByte(passengers.size.toByte())
                passengers.forEach(::writeInt)
            }.build() as PacketDataSerializer)
        }
    }

    /** 不明白作用,暂不做实现 */
    override fun createMultiBlockChange(data: PacketData): Any {
        val location = data.read<Location>("location")
        val position = SectionPosition.a(location.toPosition())
        val short = ShortSets.EMPTY_SET
        val flag = data.readOrElse("flag", false)
        return NMS16MultiBlockChange(position, short, ChunkSection(0), flag)
    }

    override fun createNamedSoundEffect(data: PacketData): Any {
        val soundName = data.read<String>("soundName")
        val location = data.read<Location>("location")
        val category = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val volume = data.readOrElse("volume", 1.0f)
        val pitch = data.readOrElse("pitch", 1.0f)
        return NMS16NamedSoundEffect(
            SoundEffect(MinecraftKey(pluginId, soundName)),
            category,
            location.x,
            location.y,
            location.z,
            volume,
            pitch
        )
    }

    override fun createNBTQuery(data: PacketData): Any {
        TODO("暂未实现操作")
    }

    override fun createOpenBook(data: PacketData): Any {
        val hand = data.readEnumOrElse(EnumHand::class.java, "hand", EnumHand.MAIN_HAND)
        return NMS16OpenBook(hand)
    }

    override fun createOpenSignEditor(data: PacketData): Any {
        val location = data.read<Location>("location")
        return NMS16OpenSignEditor(location.toPosition())
    }

    override fun createOpenWindow(data: PacketData): Any {
        val containerId = data.readOrElse("containerId", 0)
        val type = data.readOrElse("type", "")
        val title = component(data.readOrElse("title", ""))

        val clazz = NMS16OpenWindow::class.java
        return if (MinecraftVersion.versionId >= 11900) {
            NMS16OpenWindow(
                containerId,
                IRegistry.MENU.get(MinecraftKey(type)),
                title
            )
        } else if (MinecraftVersion.versionId >= 11400) {
            clazz.invokeConstructor(
                containerId,
                IRegistry.MENU.get(MinecraftKey(type)),
                title
            )
        } else {
            val size = data.readOrElse("containerSize", 0)
            clazz.invokeConstructor(containerId, type, title, size)
        }
    }

    override fun createOpenWindowHorse(data: PacketData): Any {
        val windowId = data.read<Int>("windowId")
        val slot = data.readOrElse("slot", 0)
        val entityId = data.read<Int>("entityId")
        return NMS16OpenWindowHorse(windowId, slot, entityId)
    }

    override fun createOpenWindowMerchant(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createPlayerListHeaderFooter(data: PacketData): Any {
        val header = component(data.readOrElse("header", ""))
        val footer = component(data.readOrElse("footer", ""))
        return NMS16PlayerListHeaderFooter().also {
            it.setProperty("header", header)
            it.setProperty("footer", footer)
        }
    }

    override fun createPosition(data: PacketData): Any {
        val location = data.read<Location>("location")
        val teleportFlags = data.readOrElse("teleportFlags", emptyList<String>()).map {
            PacketPlayOutPosition.EnumPlayerTeleportFlags.valueOf(it)
        }.toSet()
        val entityId = data.read<Int>("entityId")
        return NMS16Position(
            location.x,
            location.y,
            location.z,
            mathRot(location.yaw),
            mathRot(location.pitch),
            teleportFlags,
            entityId
        )
    }

    override fun createRecipeUpdate(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createRemoveEntityEffect(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val effectId = data.read<Int>("effectId")
        return NMS16RemoveEntityEffect(entityId, MobEffectList.fromId(effectId))
    }

    override fun createRespawn(data: PacketData): Any {
        val world = data.read<org.bukkit.World>("world")
        val type = when (world.environment.name) {
            "THE_END" -> World.THE_END
            "THE_NETHER" -> World.THE_NETHER
            "OVERWORLD" -> World.OVERWORLD
            else -> World.OVERWORLD
        }

        val dimension = (world as CraftWorld).handle.dimensionManager
        val seed = world.seed
        val previous = data.readEnumOrElse(EnumGamemode::class.java, "previousGamemode", EnumGamemode.SURVIVAL)
        val gamemode = data.readEnumOrElse(EnumGamemode::class.java, "gamemode", EnumGamemode.SURVIVAL)
        val isDebug = data.readOrElse("isDebug", false)
        val isFlat = data.readOrElse("isFlag", false)
        val isCopy = data.readOrElse("isCopy", false)
        return NMS16Respawn(dimension, type, seed, previous, gamemode, isDebug, isFlat, isCopy)
    }

    override fun createScoreboardDisplayObjective(data: PacketData): Any {
        val objectiveName = data.read<String>("name")
        val position = data.readOrElse("position", 0)
        val criteria = data.readOrNull<String>("criteria")

        return if (version >= 11300) {
            val healthDisplay = data.readEnumOrElse(
                NMS16ScoreboardHealthDisplay::class.java,
                "healthDisplay",
                NMS16ScoreboardHealthDisplay.INTEGER
            )
            val displayName = component(data.readOrElse("displayName", ""))
            val objective = NMS16ScoreboardObjective(
                NMS16Scoreboard(),
                objectiveName,
                NMS16IScoreboardCriteria.a(criteria?.uppercase() ?: "AIR").get(),
                displayName,
                healthDisplay
            )
            NMS16ScoreboardDisplayObjective(position, objective)
        } else {
            val objective = NMS12ScoreboardObjective(
                NMS12Scoreboard(),
                objectiveName,
                NMS12IScoreboardCriteria.criteria[criteria ?: "air"]
            )
            NMS12ScoreboardDisplayObjective(position, objective)
        }
    }

    override fun createScoreboardObjective(data: PacketData): Any {
        val objectiveName = data.read<String>("objectiveName")
        val criteria = data.readOrElse("criteria", "AIR").uppercase()
        val healthDisplay = data.readEnumOrElse(
            NMS16ScoreboardHealthDisplay::class.java,
            "healthDisplay",
            NMS16ScoreboardHealthDisplay.INTEGER
        )
        val displayName = component(data.readOrElse("displayName", ""))
        val objective = NMS16ScoreboardObjective(
            NMS16Scoreboard(),
            objectiveName,
            NMS16IScoreboardCriteria.a(criteria).get(),
            displayName,
            healthDisplay
        )
        val action = data.read<Int>("action")
        return PacketPlayOutScoreboardObjective(objective, action)
    }

    override fun createScoreboardScore(data: PacketData): Any {
        NMS12ScoreboardScore()
        val action = data.readEnumOrElse(ScoreboardServer.Action::class.java, "action", ScoreboardServer.Action.CHANGE)
        val objectiveName = data.read<String>("objectiveName")
        val score = data.readOrElse("score", 0)
        val name = data.read<String>("name")
        return NMS16ScoreboardScore(action, objectiveName, name, score)
    }

    override fun createScoreboardTeam(data: PacketData): Any {
        val uniqueName = data.read<String>("name")
        val team = data.bind(ScoreboardTeam(Scoreboard(), uniqueName)).readNotNull<String>("prefix") {
            prefix = component(it)
        }.readNotNull<String>("suffix") {
            suffix = component(it)
        }.get()
        val mode = data.read<Int>("mode")
        return NMS16ScoreboardTeam(team, mode)
    }

    override fun createSelectAdvancementTab(data: PacketData): Any {
        val advancement = data.read<String>("advancement")
        return NMS16SelectAdvancementTab(MinecraftKey(advancement))
    }

    override fun createServerDifficulty(data: PacketData): Any {
        val difficulty = data.readEnumOrElse(EnumDifficulty::class.java, "difficulty", EnumDifficulty.PEACEFUL)
        val isLocked = data.readOrElse("locked", false)
        return NMS16ServerDifficulty(difficulty, isLocked)
    }

    override fun createSetCooldown(data: PacketData): Any {
        val item = data.read<org.bukkit.inventory.ItemStack>("item")
        val cooldown = data.read<Int>("cooldown")
        return NMS16SetCooldown(toNMSItem(item).item, cooldown)
    }

    override fun createSetSlot(data: PacketData): Any {
        val containerId = data.readOrElse("containerId", 0)
        val slot = data.readOrElse("slot", 0)
        val item = toNMSItem(data.readOrElse("item", org.bukkit.inventory.ItemStack(Material.AIR)))
        return NMS16SetSlot(containerId, slot, item)
    }

    override fun createSpawnPosition(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val id = data.readOrElse("id", 0f)
        return NMS16SpawnPosition(location, id)
    }

    override fun createStatistic(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createStopSound(data: PacketData): Any {
        val key = MinecraftKey(data.read<String>("key"))
        val sound = data.readEnumOrElse(SoundCategory::class.java, "sound", SoundCategory.PLAYERS)
        return NMS16StopSound(key, sound)
    }

    override fun createOutTabComplete(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
        val command = data.read<String>("command")
        val suggestions = data.read<List<TabCompleter>>("suggestion")
        Suggestions.create(command, listOf<Suggestion>())
        return NMS16TabComplete()
    }

    override fun createTileEntityData(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val action = data.read<Int>("action")
        val nbt = data.read<ItemTagData>("nbt").saveToString()
        return NMS16TileEntityData(location, action, MojangsonParser.parse(nbt))
    }

    override fun createUnloadChunk(data: PacketData): Any {
        val x = data.readOrElse("x", 0)
        val z = data.readOrElse("z", 0)
        return NMS16UnloadChunk(x, z)
    }

    override fun createUpdateAttributes(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val attributes = data.read<List<Attribute>>("attributes").map {
            AttributeModifiable(CraftAttributeMap.toMinecraft(it)) {}
        }
        return NMS16UpdateAttributes(entityId, attributes)
    }

    override fun createUpdateHealth(data: PacketData): Any {
        val food = data.read<Int>("food")
        val health = data.read<Float>("health")
        val foodSaturation = data.read<Float>("foodSaturation")
        check(foodSaturation in 0.0..5.0) { "饱和度必须在0~5之间" }
        return NMS16UpdateHealth(health, food, foodSaturation)
    }

    override fun createUpdateTime(data: PacketData): Any {
        val tick = data.read<Long>("tick")
        val time = data.read<Long>("time")
        val isIncreasing = data.readOrElse("increasing", false)
        return NMS16UpdateTime(tick, time, isIncreasing)
    }

    override fun createVehicleMove(data: PacketData): Any {
        val location = data.read<Location>("location")
        return NMS16VehicleMove().also {
            it.a(createDataSerializer {
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeFloat(mathRot(location.yaw))
                writeFloat(mathRot(location.pitch))
            }.build() as PacketDataSerializer)
        }
    }

    override fun createViewCentre(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createViewDistance(data: PacketData): Any {
        val distance = data.read<Int>("distance")
        return NMS16ViewDistance(distance)
    }

    override fun createWindowData(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val id = data.read<Int>("id")
        val value = data.read<Int>("value")
        return NMS16WindowData(containerId, id, value)
    }

    override fun createWindowItems(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val items = NonNullList.a<ItemStack>()
        data.readOrElse("items", emptyList<org.bukkit.inventory.ItemStack>()).forEach { i ->
            items.add(toNMSItem(i))
        }
        return NMS16WindowItems(containerId, items)
    }

    override fun createWorldEvent(data: PacketData): Any {
        val type = data.read<Int>("type")
        val location = data.readOrElse("location", emptyLocation)
        val dataValue = data.readOrElse("dataValue", 0)
        val flag = data.readOrElse("flag", false)
        return NMS16WorldEvent(type, location.toPosition(), dataValue, flag)
    }

    override fun createWorldParticles(data: PacketData): Any {
        val type = CraftParticle.toNMS(data.read<Particle>("type"))
        val overrideLimiter = data.readOrElse("overrideLimiter", false)
        val location = data.read<Location>("location")
        val vector = data.readOrElse("vector", Vector())
        val maxSpeed = data.readOrElse("maxSpeed", 1.0f)
        val count = data.readOrElse("count", 1)

        return PacketPlayOutWorldParticles(
            type,
            overrideLimiter,
            location.x,
            location.y,
            location.z,
            vector.x.toFloat(),
            vector.y.toFloat(),
            vector.z.toFloat(),
            maxSpeed,
            count
        )
    }

    override fun createNamedEntitySpawn(data: PacketData): Any {
        val id = data.read<Int>("id")
        val uuid = data.read<UUID>("uuid")
        val location = data.readOrElse("location", emptyLocation)
        return NMS16NamedEntitySpawn().also {
            it.a(createDataSerializer {
                writeInt(id)
                writeUUID(uuid)
                writeDouble(location.x)
                writeDouble(location.y)
                writeDouble(location.z)
                writeFloat(mathRot(location.yaw))
                writeFloat(mathRot(location.pitch))
            }.build() as PacketDataSerializer)
        }
    }

    override fun createSpawnEntityPainting(data: PacketData): Any {
        val type = data.read<String>("type")
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val direction = data.readEnumOrElse(EnumDirection::class.java, "direction", EnumDirection.NORTH)
        val location = data.readOrElse("location", emptyLocation)
        return NMS16SpawnEntityPainting().also {
            it.a(createDataSerializer {
                writeInt(entityId)
                writeUUID(uuid)
                try {
                    writeInt(IRegistry.MOTIVE.a(Paintings::class.java.getField(type).get(null) as? Paintings))
                } catch (_: NoSuchFieldError) {
                    writeString(type)
                }
                writeBlockPosition(location.blockX, location.blockY, location.blockZ)
                writeByte(direction.c().toByte())
            }.build() as PacketDataSerializer)
        }
    }

    private fun component(text: String): IChatBaseComponent {
        return if (text.startsWith("{") && text.endsWith("}")) {
            if (require(IChatBaseComponent.ChatSerializer::class.java)) {
                listOf(
                    { CraftChatMessage.fromJSON(text) },
                    {
                        IChatBaseComponent.ChatSerializer::class.java.invokeMethod<Any>(
                            "fromJson",
                            text,
                            IRegistryCustom::class.java.getProperty<Any>("EMPTY", true),
                            true
                        )!!
                    }
                ).firstNotNullOf { runCatching(it).getOrNull() as? IChatBaseComponent }
            } else {
                CraftChatMessage.fromJSON(text)
            }
        } else {
            ChatComponentText(text)
        } as IChatBaseComponent
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    fun Location.toPosition(): BlockPosition {
        return BlockPosition(blockX, blockY, blockZ)
    }

}