package org.craft.packetfactory.packet

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntMaps
import it.unimi.dsi.fastutil.shorts.ShortSets
import net.minecraft.commands.arguments.ArgumentAnchor
import net.minecraft.core.*
import net.minecraft.core.particles.ParticleParam
import net.minecraft.nbt.MojangsonParser
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.chat.ChatMessageType
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.DataWatcher
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.resources.MinecraftKey
import net.minecraft.server.ScoreboardServer
import net.minecraft.server.bossevents.BossBattleCustom
import net.minecraft.sounds.SoundCategory
import net.minecraft.sounds.SoundEffect
import net.minecraft.stats.Statistic
import net.minecraft.world.BossBattle
import net.minecraft.world.EnumDifficulty
import net.minecraft.world.EnumHand
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityPose
import net.minecraft.world.entity.EnumItemSlot
import net.minecraft.world.entity.ai.attributes.AttributeModifiable
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.entity.player.PlayerAbilities
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.EnumGamemode
import net.minecraft.world.level.block.state.IBlockData
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.chunk.ChunkSection
import net.minecraft.world.level.saveddata.maps.MapIcon
import net.minecraft.world.phys.Vec3D
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.ScoreboardObjective
import net.minecraft.world.scores.ScoreboardTeam
import net.minecraft.world.scores.criteria.IScoreboardCriteria
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle
import org.bukkit.craftbukkit.v1_17_R1.CraftStatistic
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.attribute.CraftAttributeMap
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import org.craft.packetfactory.data.Attribute
import org.craft.packetfactory.data.MapData
import org.craft.packetfactory.data.PacketData
import org.craft.packetfactory.data.PlayerData
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.remap.require
import java.util.*
import kotlin.jvm.optionals.getOrNull

internal class NMS17 : NMSOut {
    /**
     * 创建实体生成数据包
     *
     * @param data 包含实体生成信息的数据对象，需要包含以下字段：
     *             - entityType: String 实体类型名称（必需）
     *             - entityId: Int 实体ID（必需）
     *             - uuid: UUID 实体UUID（必需）
     *             - location: Location 生成位置（必需）
     *             - data: Int 实体数据（可选，默认0）
     * @return PacketPlayOutSpawnEntity 实体生成数据包实例
     */
    override fun createSpawnEntity(data: PacketData): Any {
        val entityType = data.read<String>("entityType")
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")

        val location = data.read<Location>("location")
        val data = data.readOrElse("data", 0)
        val type = IRegistry.ENTITY_TYPE.get(MinecraftKey(entityType.uppercase()))
        return PacketPlayOutSpawnEntity(
            entityId, uuid, location.x, location.y, location.z, location.yaw, location.pitch, type, data, Vec3D.ZERO
        )
    }

    /**
     * 创建生物实体生成数据包
     *
     * @param data 包含生物实体生成信息的数据对象，需要包含以下字段：
     *             - entityId: Int 实体ID（必需）
     *             - uuid: UUID 实体UUID（必需）
     *             - entityType: EntityType 生物实体类型（必需）
     *             - location: Location 生成位置（必需）
     *             - yHeadRot: Int 头部旋转角度（可选，默认0）
     * @return PacketPlayOutSpawnEntityLiving 生物实体生成数据包实例
     */
    override fun createSpawnEntityLiving(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val entityType = data.read<EntityType>("entityType")
        val location = data.read<Location>("location")
        val yaw = mathRot(fixYaw(entityType, location.yaw))
        val pitch = mathRot(location.pitch)
        val yHeadRot = data.readOrElse("yHeadRot", 0).toByte()

        return PacketPlayOutSpawnEntityLiving::class.java.unsafeInstance().also {
            it.setProperty("id", entityId)
            it.setProperty("uuid", uuid)
            it.setProperty("type", entityType.typeId.toInt())
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("yRot", yaw.toByte())
            it.setProperty("xRot", pitch.toByte())
            it.setProperty("yHeadRot", yHeadRot)
        }
    }

    /**
     * 创建经验球实体生成数据包
     *
     * @param data 包含经验球生成信息的数据对象，需要包含以下字段：
     *             - id: Int 经验球ID（必需）
     *             - location: Location 生成位置（必需）
     *             - value: Int 经验值（可选，默认0）
     * @return PacketPlayOutSpawnEntityExperienceOrb 经验球实体生成数据包实例
     */
    override fun createSpawnEntityExperienceOrb(data: PacketData): Any {
        val id = data.read<Int>("id")
        val location = data.read<Location>("location")
        val value = data.readOrElse("value", 0)
        return PacketPlayOutSpawnEntityExperienceOrb::class.java.unsafeInstance().also {
            it.setProperty("id", id)
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("value", value)
        }
    }

    override fun createTeleportPosition(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        val onGround = data.read<Boolean>("onGround")
        return PacketPlayOutEntityTeleport::class.java.unsafeInstance().also {
            it.setProperty("id", entityId)
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("yRot", mathRot(location.pitch))
            it.setProperty("xRot", mathRot(location.yaw))
            it.setProperty("onGround", onGround)
        }
    }

    override fun createEntityHeadRotation(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val pitch = data.read<Float>("pitch")
        return PacketPlayOutEntityHeadRotation::class.java.unsafeInstance().also {
            it.setProperty("id", entityId)
            it.setProperty("yHeadRot", mathRot(pitch))
        }
    }

    override fun createEntityMetadata(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val metadata = data.read<List<Any>>("metadata").map { getDataWatcherItem(it) }
        return PacketPlayOutEntityMetadata::class.java.unsafeInstance().also {
            it.setProperty("id", entityId)
            it.setProperty("packedItems", metadata)
        }
    }

    override fun createEntityDestroy(data: PacketData): Any {
        val entitys = IntArrayList(data.read<List<Int>>("entitys"))
        return PacketPlayOutEntityDestroy(entitys)
    }

    override fun createRelEntityMove(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutRelEntityMove(
            entityId, location.blockX.toShort(), location.blockY.toShort(), location.blockZ.toShort(), onGround
        )
    }

    override fun createEntityLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val yaw = data.read<Byte>("yaw")
        val pitch = data.read<Byte>("pitch")
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, pitch, yaw, onGround)
    }

    override fun createRelEntityMoveLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
            entityId,
            location.x.toInt().toShort(),
            location.y.toInt().toShort(),
            location.z.toInt().toShort(),
            location.pitch.toInt().toByte(),
            location.yaw.toInt().toByte(),
            onGround
        )
    }

    override fun createEntityEquipment(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val equipments = data.read<Map<String, org.bukkit.inventory.ItemStack>>("equipments").map {
            Pair(EnumItemSlot.valueOf(it.key.uppercase()), toNMSItem(it.value))
        }
        return PacketPlayOutEntityEquipment(entityId, equipments)
    }

    override fun createAnimation(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val action = data.read<Int>("action")
        return PacketPlayOutAnimation::class.java.unsafeInstance().also {
            it.setProperty("id", entityId)
            it.setProperty("action", action)
        }
    }

    override fun createAttachEntity(data: PacketData): Any {
        val sourceId = data.read<Int>("sourceId")
        val destId = data.read<Int>("destId")
        return PacketPlayOutAttachEntity::class.java.unsafeInstance().also {
            it.setProperty("sourceId", sourceId)
            it.setProperty("destId", destId)
        }
    }

    override fun createEntityVelocity(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val vector = data.read<Vector>("vector")
        return PacketPlayOutEntityVelocity(entityId, Vec3D(vector.x, vector.y, vector.z))
    }

    override fun createBed(data: PacketData): Any {
        unsupported()
    }

    override fun createClearDialog(data: PacketData): Any {
        unsupported()
    }

    override fun createKeepAlive(data: PacketData): Any {
        return PacketPlayOutKeepAlive(data.read<Long>("id"))
    }

    override fun createPing(data: PacketData): Any {
        return ClientboundPingPacket(data.read<Int>("id"))
    }

    override fun createResourcePackPop(data: PacketData): Any {
        unsupported()
    }

    override fun createResourcePackPush(data: PacketData): Any {
        unsupported()
    }

    override fun createServerLinks(data: PacketData): Any {
        unsupported()
    }

    override fun createShowDialog(data: PacketData): Any {
        unsupported()
    }

    override fun createStoreCookie(data: PacketData): Any {
        unsupported()
    }

    override fun createTransfer(data: PacketData): Any {
        unsupported()
    }

    override fun createUpdateTags(data: PacketData): Any {
        unsupported()
    }

    override fun createClientInformation(data: PacketData): Any {
        unsupported()
    }

    override fun createCustomClickAction(data: PacketData): Any {
        unsupported()
    }

    override fun createCustomPayload(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现")
    }

    override fun createPong(data: PacketData): Any {
        unsupported()
    }

    override fun createResourcePack(data: PacketData): Any {
        unsupported()
    }

    override fun createCodeOfConduct(data: PacketData): Any {
        unsupported()
    }

    override fun createFinishConfiguration(data: PacketData): Any {
        unsupported()
    }

    override fun createRegistryData(data: PacketData): Any {
        unsupported()
    }

    override fun createResetChat(data: PacketData): Any {
        unsupported()
    }

    override fun createSelectKnown(data: PacketData): Any {
        unsupported()
    }

    override fun createUpdateEnabledFeatures(data: PacketData): Any {
        unsupported()
    }

    override fun createAcceptCodeOfConduct(data: PacketData): Any {
        unsupported()
    }

    override fun createCookieRequest(data: PacketData): Any {
        unsupported()
    }

    override fun createCookieResponse(data: PacketData): Any {
        unsupported()
    }

    override fun createBlockChangedAck(data: PacketData): Any {
        unsupported()
    }

    override fun createBundleDelimiter(data: PacketData): Any {
        unsupported()
    }

    override fun createChunkBatchFinished(data: PacketData): Any {
        unsupported()
    }

    override fun createChunkBatchStart(data: PacketData): Any {
        unsupported()
    }

    override fun createChunksBiomes(data: PacketData): Any {
        unsupported()
    }

    override fun createClearTitles(data: PacketData): Any {
        return ClientboundClearTitlesPacket(data.readOrElse("clear", true))
    }

    override fun createCustomChatCompletions(data: PacketData): Any {
        unsupported()
    }

    override fun createDamageEvent(data: PacketData): Any {
        unsupported()
    }

    override fun createDebugBlockValue(data: PacketData): Any {
        unsupported()
    }

    override fun createDebugChunkValue(data: PacketData): Any {
        unsupported()
    }

    override fun createDebugEntityValue(data: PacketData): Any {
        unsupported()
    }

    override fun createDebugEvent(data: PacketData): Any {
        unsupported()
    }

    override fun createDebugSample(data: PacketData): Any {
        unsupported()
    }

    override fun createDeleteChat(data: PacketData): Any {
        unsupported()
    }

    override fun createDisguisedChat(data: PacketData): Any {
        unsupported()
    }

    override fun createEntityPositionSync(data: PacketData): Any {
        unsupported()
    }

    override fun createGameTestHighlightPos(data: PacketData): Any {
        unsupported()
    }

    override fun createHurtAnimation(data: PacketData): Any {
        unsupported()
    }

    override fun createInitializeBorder(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现")
    }

    override fun createLevelChunkWithLight(data: PacketData): Any {
        unsupported()
    }

    override fun createMoveMinecart(data: PacketData): Any {
        unsupported()
    }

    override fun createPlayerChat(data: PacketData): Any {
        val text = component(data.read("text"))
        val type = data.readEnum(ChatMessageType::class.java, "text")
        val uuid = data.read<UUID>("uuid")
        return PacketPlayOutChat(text, type, uuid)
    }

    override fun createPlayerCombatEnd(data: PacketData): Any {
        val killer = data.read<Int>("killer")
        val duration = data.read<Int>("duration")
        return ClientboundPlayerCombatEndPacket(killer, duration)
    }

    override fun createPlayerCombatEnter(data: PacketData): Any {
        return ClientboundPlayerCombatEnterPacket()
    }

    override fun createPlayerCombatKill(data: PacketData): Any {
        val player = data.read<Int>("player")
        val killer = data.read<Int>("killer")
        val message = component(data.read("text"))
        return ClientboundPlayerCombatKillPacket(player, killer, message)
    }

    override fun createPlayerInfoRemove(data: PacketData): Any {
        val players = data.read<List<PlayerData>>("players").map {
            PacketPlayOutPlayerInfo.PlayerInfoData(GameProfile(it.uuid, it.name), it.ping, EnumGamemode.valueOf(it.gamemode.name), null)
        }
        return PacketPlayOutPlayerInfo::class.java.unsafeInstance().also {
            it.setProperty("action", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER)
            it.setProperty("entries", players)
        }
    }

    override fun createPlayerInfoUpdate(data: PacketData): Any {
        val type = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.valueOf("UPDATE_" + data.read<String>("type").uppercase())
        val players = data.read<List<PlayerData>>("players").map {
            PacketPlayOutPlayerInfo.PlayerInfoData(GameProfile(it.uuid, it.name), it.ping, EnumGamemode.valueOf(it.gamemode.name), null)
        }
        return PacketPlayOutPlayerInfo::class.java.unsafeInstance().also {
            it.setProperty("action", type)
            it.setProperty("entries", players)
        }
    }

    override fun createPlayerRotation(data: PacketData): Any {
        unsupported()
    }

    override fun createProjectilePower(data: PacketData): Any {
        unsupported()
    }

    override fun createRecipeBookAdd(data: PacketData): Any {
        unsupported()
    }

    override fun createRecipeBookRemove(data: PacketData): Any {
        unsupported()
    }

    override fun createRecipeBookSettings(data: PacketData): Any {
        unsupported()
    }

    override fun createResetScore(data: PacketData): Any {
        unsupported()
    }

    override fun createServerData(data: PacketData): Any {
        unsupported()
    }

    override fun createSetActionBarText(data: PacketData): Any {
        val text = component(data.read("text"))
        return ClientboundSetActionBarTextPacket(text)
    }

    override fun createSetBorderCenter(data: PacketData): Any {
        val border = WorldBorder()
        border.setCenter(data.read("x"), data.read("z"))
        return ClientboundSetBorderCenterPacket(border)
    }

    override fun createSetBorderLerpSize(data: PacketData): Any {
        val oldSize = data.read<Double>("oldSize")
        val newSize = data.read<Double>("newSize")
        val lerpTime = data.read<Int>("lerpTime")
        return ClientboundSetBorderLerpSizePacket(WorldBorder()).also {
            it.setProperty("oldSize", oldSize)
            it.setProperty("newSize", newSize)
            it.setProperty("lerpTime", lerpTime)
        }
    }

    override fun createSetBorderSize(data: PacketData): Any {
        return ClientboundSetBorderSizePacket(WorldBorder()).also {
            it.setProperty("size", data.read<Double>("size"))
        }
    }

    override fun createSetBorderWarningDelay(data: PacketData): Any {
        val border = WorldBorder()
        border.warningTime = data.read("warningTime")
        return ClientboundSetBorderWarningDelayPacket(border)
    }

    override fun createSetBorderWarningDistance(data: PacketData): Any {
        val border = WorldBorder()
        border.warningDistance = data.read("warningDistance")
        return ClientboundSetBorderWarningDistancePacket(border)
    }

    override fun createSetCursorItem(data: PacketData): Any {
        unsupported()
    }

    override fun createSetPlayerInventory(data: PacketData): Any {
        unsupported()
    }

    override fun createSetSimulationDistance(data: PacketData): Any {
        unsupported()
    }

    override fun createSetSubtitleText(data: PacketData): Any {
        val text = component(data.read("text"))
        return ClientboundSetSubtitleTextPacket(text)
    }

    override fun createSetTitlesAnimation(data: PacketData): Any {
        val fadeIn = data.read<Int>("fadeIn")
        val stay = data.read<Int>("stay")
        val fadeOut = data.read<Int>("fadeOut")
        return ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut)
    }

    override fun createSetTitleText(data: PacketData): Any {
        return ClientboundSetTitleTextPacket(component(data.read("title")))
    }

    override fun createStartConfiguration(data: PacketData): Any {
        unsupported()
    }

    override fun createSystemChat(data: PacketData): Any {
        return PacketPlayOutChat(component(data.read("text")), ChatMessageType.SYSTEM, data.read("uuid"))
    }

    override fun createTestInstanceBlockStatus(data: PacketData): Any {
        unsupported()
    }

    override fun createTickingState(data: PacketData): Any {
        unsupported()
    }

    override fun createTickingStep(data: PacketData): Any {
        unsupported()
    }

    override fun createTrackedWaypoint(data: PacketData): Any {
        unsupported()
    }

    override fun createAbilities(data: PacketData): Any {
        val abilities = data.bind(PlayerAbilities()).readNotNull<Boolean>("invulnerable") {
            invulnerable = it
        }.readNotNull<Boolean>("isFlying") {
            flying = it
        }.readNotNull<Boolean>("canFly") {
            mayfly = it
        }.readNotNull<Boolean>("instabuild") {
            instabuild = it
        }.readNotNull<Float>("flyingSpeed") {
            flyingSpeed = it
        }.readNotNull<Float>("walkingSpeed") {
            walkingSpeed = it
        }.get()
        return PacketPlayOutAbilities(abilities)
    }

    override fun createAdvancements(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现")
    }

    override fun createAutoRecipe(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现")
    }

    override fun createBlockAction(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val block = IRegistry.BLOCK[MinecraftKey(data.read<Block>("block").type.name)]
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        return PacketPlayOutBlockAction(location, block, x, z)
    }

    override fun createBlockBreakAnimation(data: PacketData): Any {
        val id = data.read<Int>("id")
        val location = data.read<Location>("location").toPosition()
        val progress = data.read<Int>("progress")
        return PacketPlayOutBlockBreakAnimation(id, location, progress)
    }

    override fun createBlockChange(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现")
    }

    override fun createBossBar(data: PacketData): Any {
        val boss = BossBattleCustom(MinecraftKey(pluginId, "bossbar"), component(data.read("text")))
        return when (data.read<String>("action")) {
            "add" -> {
                data.bind(boss).readNotNull<String>("name") {
                    name = component(it)
                }.readNotNull<Float>("progress") {
                    progress = it
                }.readNotNull<String>("color") {
                    color = BossBattle.BarColor.valueOf(it.uppercase())
                }.readNotNull<String>("style") {
                    overlay = BossBattle.BarStyle.valueOf(it.uppercase())
                }.readNotNull<Boolean>("darkenScreen") {
                    isDarkenSky = it
                }.readNotNull<Boolean>("playMusic") {
                    isPlayMusic = it
                }.readNotNull<Boolean>("createWorldFog") {
                    isCreateFog = it
                }
                PacketPlayOutBoss.createAddPacket(boss)
            }

            "remove" -> {
                val uuid = data.read<UUID>("uuid")
                PacketPlayOutBoss.createRemovePacket(uuid)
            }

            "progress" -> {
                data.bind(boss).readNotNull<Float>("progress") {
                    progress = it
                }
                PacketPlayOutBoss.createUpdateProgressPacket(boss)
            }

            "name" -> {
                data.bind(boss).readNotNull<String>("name") {
                    name = component(it)
                }
                PacketPlayOutBoss.createUpdateNamePacket(boss)
            }

            "style" -> {
                data.bind(boss).readNotNull<String>("color") {
                    color = BossBattle.BarColor.valueOf(it.uppercase())
                }.readNotNull<String>("overlay") {
                    overlay = BossBattle.BarStyle.valueOf(it.uppercase())
                }
                PacketPlayOutBoss.createUpdateStylePacket(boss)
            }

            "properties" -> {
                data.bind(boss).readNotNull<Boolean>("darkenSky") {
                    isDarkenSky = it
                }.readNotNull<Boolean>("playMusic") {
                    isPlayMusic = it
                }.readNotNull<Boolean>("createFog") {
                    isCreateFog = it
                }
                PacketPlayOutBoss.createUpdatePropertiesPacket(boss)
            }

            else -> error("不支持的类型")
        }
    }

    override fun createCamera(data: PacketData): Any {
        return PacketPlayOutCamera::class.java.unsafeInstance().also {
            it.setProperty("cameraId", data.read<Int>("cameraId"))
        }
    }

    override fun createCloseWindow(data: PacketData): Any {
        return PacketPlayOutCloseWindow(data.read<Int>("containerId"))
    }

    override fun createCollect(data: PacketData): Any {
        val itemId = data.read<Int>("itemId")
        val playerId = data.read<Int>("playerId")
        val amount = data.read<Int>("amount")
        return PacketPlayOutCollect(itemId, playerId, amount)
    }

    override fun createCommands(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityEffect(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val effect = IRegistry.MOB_EFFECT.fromId(data.read("effectId"))
        val duration = data.readOrElse("duration", 0)
        val amplifier = data.readOrElse("amplifier", 0)
        val ambient = data.readOrElse("ambient", false)
        val visible = data.readOrElse("visible", true)
        val showIcon = data.readOrElse("showIcon", true)
        return PacketPlayOutEntityEffect(entityId, MobEffect(effect, duration, amplifier, ambient, visible, showIcon))
    }

    override fun createEntitySound(data: PacketData): Any {
        val sound = data.read<String>("sound")
        val soundEffect = SoundEffect(MinecraftKey(pluginId, "entity_sound_$sound"))
        val category = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val entityId = data.read<Int>("entityId")
        val volume = data.readOrElse("volume", 0.0f)
        val pitch = data.readOrElse("pitch", 0.0f)
        return PacketPlayOutEntitySound::class.java.unsafeInstance().also {
            it.setProperty("sound", soundEffect)
            it.setProperty("source", category)
            it.setProperty("id", entityId)
            it.setProperty("volume", volume)
            it.setProperty("pitch", pitch)
        }
    }

    override fun createEntityStatus(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val eventId = data.read<Int>("eventId")
        return PacketPlayOutEntityStatus::class.java.unsafeInstance().also {
            it.setProperty("entityId", entityId)
            it.setProperty("eventId", eventId)
        }
    }

    override fun createExplosion(data: PacketData): Any {
        val location = data.read<Location>("location")
        val power = data.read<Float>("power")
        val positions = data.read<List<Location>>("positions").map { it.toPosition() }
        val vector = data.read<Vector>("vector")
        return PacketPlayOutExplosion(location.x, location.y, location.z, power, positions, Vec3D(vector.x, vector.y, vector.z))
    }

    override fun createGameStateChange(data: PacketData): Any {
        val change = data.read<Int>("change")
        val param = data.read<Float>("param")
        return PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.a(change), param)
    }

    override fun createHeldItemSlot(data: PacketData): Any {
        return PacketPlayOutHeldItemSlot(data.read<Int>("slot"))
    }

    override fun createLightUpdate(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createLogin(data: PacketData): Any {
        TODO("该包目前不知道什么作用,参数过长暂时不做")
    }

    override fun createLookAt(data: PacketData): Any {
        val location = data.read<Location>("location")
        val anchor = data.readEnum(ArgumentAnchor.Anchor::class.java, "anchor")
        return PacketPlayOutLookAt(anchor, location.x, location.y, location.z)
    }

    override fun createMap(data: PacketData): Any {
        val mapId = data.read<Int>("mapId")
        val scale = data.read<Byte>("scale")
        val locked = data.read<Boolean>("locked")
        val maps = data.read<List<MapData>>("maps").map {
            MapIcon(MapIcon.Type.valueOf(it.type), it.x, it.z, it.rotation, component(it.name))
        }
        return PacketPlayOutMap(mapId, scale, locked, maps, null)
    }

    override fun createMount(data: PacketData): Any {
        val vehicle = data.read<Int>("vehicle")
        val passengers = data.read<List<Int>>("passengers")
        return PacketPlayOutMount::class.java.unsafeInstance().also {
            it.setProperty("vehicle", vehicle)
            it.setProperty("passengers", passengers.toTypedArray())
        }
    }

    override fun createMultiBlockChange(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        return PacketPlayOutMultiBlockChange(SectionPosition.a(location), ShortSets.EMPTY_SET, ChunkSection(0), false)
    }

    override fun createNamedSoundEffect(data: PacketData): Any {
        val sound = data.read<String>("sound")
        val soundEffect = SoundEffect(MinecraftKey(pluginId, "entity_sound_$sound"))
        val category = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val location = data.read<Location>("location")
        val volume = data.readOrElse("volume", 0.0f)
        val pitch = data.readOrElse("pitch", 0.0f)
        return PacketPlayOutNamedSoundEffect(soundEffect, category, location.x, location.y, location.z, volume, pitch)
    }

    override fun createNBTQuery(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenBook(data: PacketData): Any {
        val hand = data.readEnum(EnumHand::class.java, "hand")
        return PacketPlayOutOpenBook(hand)
    }

    override fun createOpenSignEditor(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        return PacketPlayOutOpenSignEditor(location)
    }

    override fun createOpenWindow(data: PacketData): Any {
        val containerId = data.readOrElse("containerId", 0)
        val type = data.readOrElse("type", "")
        val title = component(data.readOrElse("title", ""))
        val container = IRegistry.MENU.get(MinecraftKey(type))
        return PacketPlayOutOpenWindow(containerId, container, title)
    }

    override fun createOpenWindowHorse(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val size = data.read<Int>("size")
        val entityId = data.read<Int>("entityId")
        return PacketPlayOutOpenWindowHorse(containerId, size, entityId)
    }

    override fun createOpenWindowMerchant(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerListHeaderFooter(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPosition(data: PacketData): Any {
        val location = data.read<Location>("location")
        val teleportFlags = data.readOrElse("teleportflags", listOf<String>()).map {
            PacketPlayOutPosition.EnumPlayerTeleportFlags.valueOf(it)
        }.toSet()
        val id = data.read<Int>("id")
        val dismountVehicle = data.readOrElse("dismountVehicle", false)
        return PacketPlayOutPosition(
            location.x, location.y, location.z, mathRot(location.pitch).toFloat(),
            mathRot(location.yaw).toFloat(), teleportFlags, id, dismountVehicle
        )
    }

    override fun createRecipeUpdate(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRemoveEntityEffect(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val effect = IRegistry.MOB_EFFECT[MinecraftKey(data.read("effect"))]
        return PacketPlayOutRemoveEntityEffect(entityId, effect)
    }

    override fun createRespawn(data: PacketData): Any {
        val world = data.read<org.bukkit.World>("world") as CraftWorld
        val type = world.handle.dimensionKey
        val gamemode = data.readEnumOrElse(EnumGamemode::class.java, "gamemode", EnumGamemode.SURVIVAL)
        val previoudGamemode = data.readEnumOrElse(EnumGamemode::class.java, "previousGameMode", EnumGamemode.SURVIVAL)
        val debug = world.handle.isDebugWorld
        val flat = world.handle.isFlatWorld
        val keepSpawn = world.keepSpawnInMemory
        return PacketPlayOutRespawn(world.handle.dimensionManager, type, world.seed, gamemode, previoudGamemode, debug, flat, keepSpawn)
    }

    override fun createScoreboardDisplayObjective(data: PacketData): Any {
        val objectiveName = data.read<String>("name")
        val slot = data.readOrElse("slot", 0)
        val criteria = data.read<String>("criteria")
        val displayName = data.read<String>("displayName")
        val healthDisplay = data.readEnum(IScoreboardCriteria.EnumScoreboardHealthDisplay::class.java, "healthDisplay")
        val objective = ScoreboardObjective(Scoreboard(), objectiveName, IScoreboardCriteria.a(criteria).get(), component(displayName), healthDisplay)
        return PacketPlayOutScoreboardDisplayObjective(slot, objective)
    }

    override fun createScoreboardObjective(data: PacketData): Any {
        val objectiveName = data.read<String>("objectiveName")
        val criteria = data.readOrElse("criteria", "AIR").uppercase()
        val healthDisplay = data.readEnumOrElse(
            IScoreboardCriteria.EnumScoreboardHealthDisplay::class.java, "healthDisplay", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER
        )
        val displayName = component(data.readOrElse("displayName", ""))
        val objective = ScoreboardObjective(
            Scoreboard(), objectiveName, IScoreboardCriteria.a(criteria).get(), displayName, healthDisplay
        )
        val action = data.read<Int>("action")
        return PacketPlayOutScoreboardObjective(objective, action)
    }

    override fun createScoreboardScore(data: PacketData): Any {
        val action = data.readEnumOrElse(ScoreboardServer.Action::class.java, "action", ScoreboardServer.Action.CHANGE)
        val objectiveName = data.read<String>("objectiveName")
        val score = data.readOrElse("score", 0)
        val name = data.read<String>("name")
        return PacketPlayOutScoreboardScore(action, objectiveName, name, score)
    }

    override fun createScoreboardTeam(data: PacketData): Any {
        val uniqueName = data.read<String>("name")
        val team = data.bind(ScoreboardTeam(Scoreboard(), uniqueName)).readNotNull<String>("prefix") {
            prefix = component(it)
        }.readNotNull<String>("suffix") {
            suffix = component(it)
        }.get()
        val mode = data.read<Int>("mode")
        val players = data.read<List<String>>("players")
        return PacketPlayOutScoreboardTeam::class.java.invokeConstructor(uniqueName, mode, Optional.of(PacketPlayOutScoreboardTeam.a(team)), players)
    }

    override fun createSelectAdvancementTab(data: PacketData): Any {
        return PacketPlayOutSelectAdvancementTab(MinecraftKey(data.read("identifier")))
    }

    override fun createServerDifficulty(data: PacketData): Any {
        val difficulty = data.readEnum(EnumDifficulty::class.java, "difficulty")
        val locked = data.read<Boolean>("locked")
        return PacketPlayOutServerDifficulty(difficulty, locked)
    }

    override fun createSetCooldown(data: PacketData): Any {
        val item = toNMSItem(data.read("item")).item
        val duration = data.read<Int>("duration")
        return PacketPlayOutSetCooldown(item, duration)
    }

    override fun createSetSlot(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val stateId = data.read<Int>("stateId")
        val slot = data.read<Int>("slot")
        val item = toNMSItem(data.read("item"))
        return PacketPlayOutSetSlot(containerId, stateId, slot, item)
    }

    override fun createSpawnPosition(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val angle = data.read<Float>("angle")
        return PacketPlayOutSpawnPosition(location, angle)
    }

    override fun createStatistic(data: PacketData): Any {
        val map = Object2IntMaps.emptyMap<Statistic<*>>()
        data.read<Map<org.bukkit.Statistic, Int>>("statistic").forEach {
            val statistic = CraftStatistic.getNMSStatistic(it.key)
            map.put(statistic, it.value)
        }
        return PacketPlayOutStatistic(map)
    }

    override fun createStopSound(data: PacketData): Any {
        val key = try {
            MinecraftKey(data.read("key"))
        } catch (_: IllegalStateException) {
            null
        }
        val category = try {
            data.readEnum(SoundCategory::class.java, "category")
        } catch (_: IllegalStateException) {
            null
        }
        return PacketPlayOutStopSound(key, category)
    }

    override fun createOutTabComplete(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTileEntityData(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val type = data.read<Int>("type")
        val nbt = data.read<ItemTagData>("nbt").toString()
        val serializer = MojangsonParser.parse(nbt)
        return PacketPlayOutTileEntityData(location, type, serializer)
    }

    override fun createUnloadChunk(data: PacketData): Any {
        return PacketPlayOutUnloadChunk(data.read("x"), data.read("z"))
    }

    override fun createUpdateAttributes(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val attributes = data.read<List<Attribute>>("attributes").map { a ->
            val attribute = CraftAttributeMap.toMinecraft(a.attribute)
            AttributeModifiable(attribute) {
                val modifier =
                    org.bukkit.attribute.AttributeModifier(attribute.name, attribute.default, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER)
                a.callback.accept(modifier)
                warning("更新属性使用了回调函数,暂未实现修改")
            }.apply {
                value = a.base
            }
        }
        return PacketPlayOutUpdateAttributes(entityId, attributes)
    }

    override fun createUpdateHealth(data: PacketData): Any {
        val health = data.read<Float>("health")
        val food = data.read<Int>("food")
        val saturation = data.read<Float>("saturation")
        return PacketPlayOutUpdateHealth(health, food, saturation)
    }

    override fun createUpdateTime(data: PacketData): Any {
        val gameTime = data.read<Long>("gameTime")
        val dayTime = data.read<Long>("dayTime")
        val flag = data.read<Boolean>("flag")
        return PacketPlayOutUpdateTime(gameTime, dayTime, flag)
    }

    override fun createVehicleMove(data: PacketData): Any {
        val location = data.read<Location>("location")
        return PacketPlayOutVehicleMove::class.java.unsafeInstance().also {
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("yRot", mathRot(location.pitch))
            it.setProperty("xRot", mathRot(location.yaw))
        }
    }

    /**
     * 创建视图中心数据包
     *
     * @param data 包含视图中心坐标信息的数据对象，需要包含以下字段：
     *             - x: Int X坐标（必需）
     *             - z: Int Z坐标（必需）
     * @return PacketPlayOutViewCentre 视图中心数据包实例
     */
    override fun createViewCentre(data: PacketData): Any {
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        return PacketPlayOutViewCentre(x, z)
    }

    /**
     * 创建客户端视图距离网络数据包
     *
     * @param data 包含视图距离信息的数据对象，需要包含以下字段：
     *             - radius: Int 渲染距离（区块半径）（必需）
     * @return PacketPlayOutViewDistance 客户端视图距离S2C数据包
     */
    override fun createViewDistance(data: PacketData): Any {
        val radius = data.read<Int>("radius")
        return PacketPlayOutViewDistance(radius)
    }

    /**
     * 创建容器数据更新网络数据包
     *
     * @param data 包含容器数据信息的数据对象，需要包含以下字段：
     *             - containerId: Int 容器网络ID（必需）
     *             - id: Int 数据属性ID（必需）
     *             - value: Int 属性数值（必需）
     * @return PacketPlayOutWindowData 容器数据更新S2C数据包
     */
    override fun createWindowData(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val id = data.read<Int>("id")
        val value = data.read<Int>("value")
        return PacketPlayOutWindowData(containerId, id, value)
    }

    /**
     * 创建容器物品同步网络数据包
     *
     * @param data 包含容器物品信息的数据对象，需要包含以下字段：
     *             - containerId: Int 容器网络ID（必需）
     *             - stateId: Int 容器状态版本（必需）
     *             - items: List<ItemStack> 容器物品槽位列表（可选）
     *             - emptyItemStack: ItemStack 空物品栈占位符（可选，默认AIR）
     * @return PacketPlayOutWindowItems 容器物品同步S2C数据包
     */
    override fun createWindowItems(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val stateId = data.read<Int>("stateId")
        val items = NonNullList.a<ItemStack>()
        data.readOrNull<List<org.bukkit.inventory.ItemStack>>("items")?.forEach { i -> items.add(toNMSItem(i)) }
        val carriedItem = toNMSItem(data.readOrElse("emptyItemStack", org.bukkit.inventory.ItemStack(Material.AIR)))
        return PacketPlayOutWindowItems(containerId, stateId, items, carriedItem)
    }

    override fun createWorldEvent(data: PacketData): Any {
        val type = data.read<Int>("type")
        val location = data.read<Location>("location")
        val dataValue = data.read<Int>("data")
        val globalEvent = data.read<Boolean>("globalEvent")
        return PacketPlayOutWorldEvent(type, location.toPosition(), dataValue, globalEvent)
    }

    override fun createWorldParticles(data: PacketData): Any {
        val type = CraftParticle.toNMS(data.read<Particle>("type"))
        val overrideLimiter = data.readOrElse("overrideLimiter", false)
        val location = data.read<Location>("location")
        val vector = data.readOrElse("vector", Vector())
        val maxSpeed = data.readOrElse("maxSpeed", 1.0f)
        val count = data.readOrElse("count", 1)

        return PacketPlayOutWorldParticles(
            type, overrideLimiter, location.x, location.y, location.z, vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat(), maxSpeed, count
        )
    }

    /**
     * 创建玩家实体生成网络数据包
     *
     * @param data 包含玩家实体生成信息的数据对象，需要包含以下字段：
     *             - entityId: Int 玩家实体网络ID（必需）
     *             - uuid: UUID 玩家UUID（必需）
     *             - location: Location 世界生成坐标（必需）
     * @return PacketPlayOutNamedEntitySpawn 玩家实体生成S2C数据包
     */
    override fun createNamedEntitySpawn(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val location = data.read<Location>("location")

        return PacketPlayOutNamedEntitySpawn::class.java.unsafeInstance().also {
            it.setProperty("entityId", entityId)
            it.setProperty("uuid", uuid)
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("yaw", mathRot(location.yaw))
            it.setProperty("pitch", mathRot(location.pitch))
        }
    }

    override fun createSpawnEntityPainting(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    private fun component(text: String): IChatBaseComponent {
        return if (text.startsWith("{") && text.endsWith("}")) {
            if (require(IChatBaseComponent.ChatSerializer::class.java)) {
                listOf({ CraftChatMessage.fromJSON(text) }, { IChatBaseComponent.ChatSerializer.a(text) }).firstNotNullOf { runCatching(it).getOrNull() }
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

    fun getDataWatcherItem(value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BYTE), value)
    }

    fun getDataWatcherItem(value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.INT), value)
    }

    fun getDataWatcherItem(value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.FLOAT), value)
    }

    fun getDataWatcherItem(value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.STRING), value)
    }

    fun getDataWatcherItem(value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.COMPONENT), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(value: Optional<*>): DataWatcher.Item<out Optional<*>> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(
                DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_COMPONENT), value as Optional<IChatBaseComponent>
            )

            is IBlockData -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BLOCK_STATE), value as Optional<IBlockData>)

            is BlockPosition -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_BLOCK_POS), value as Optional<BlockPosition>)

            is UUID -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_UUID), value as Optional<UUID>)

            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcherItem(value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.ITEM_STACK), value)
    }

    fun getDataWatcherItem(value: Boolean): DataWatcher.Item<Boolean> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BOOLEAN), value)
    }

    fun getDataWatcherItem(value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.PARTICLE), value)
    }

    fun getDataWatcherItem(value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.ROTATIONS), value)
    }

    fun getDataWatcherItem(value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BLOCK_POS), value)
    }

    fun getDataWatcherItem(value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.DIRECTION), value)
    }

    fun getDataWatcherItem(value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.COMPOUND_TAG), value)
    }

    fun getDataWatcherItem(value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.VILLAGER_DATA), value)
    }

    fun getDataWatcherItem(value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_UNSIGNED_INT), value)
    }

    fun getDataWatcherItem(value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.POSE), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getDataWatcherItem(value: Any): DataWatcher.Item<*> {
        if (value is DataWatcher.Item<*>) return value
        if (require(EntityPose::class.java) && value is EntityPose) {
            return getDataWatcherItem(value)
        }
        if (require(VillagerData::class.java) && value is VillagerData) {
            return getDataWatcherItem(value)
        }
        if (require(ParticleParam::class.java) && value is ParticleParam) {
            return getDataWatcherItem(value)
        }
        return when (value) {
            is String -> getDataWatcherItem(value)
            is Byte -> getDataWatcherItem(value)
            is Float -> getDataWatcherItem(value)
            is NBTTagCompound -> getDataWatcherItem(value)
            is OptionalInt -> getDataWatcherItem(value)
            is Optional<*> -> getOptionalDataWatcher(value)
            is EnumDirection -> getDataWatcherItem(value)
            is BlockPosition -> getDataWatcherItem(value)
            is Vector3f -> getDataWatcherItem(value)
            is Boolean -> getDataWatcherItem(value)
            is IChatBaseComponent -> getDataWatcherItem(value)
            is ItemStack -> getDataWatcherItem(value)
            is Int -> getDataWatcherItem(value)
            is org.bukkit.inventory.ItemStack -> getDataWatcherItem(toNMSItem(value))
            else -> error("不支持的类型: $value")
        }
    }
}