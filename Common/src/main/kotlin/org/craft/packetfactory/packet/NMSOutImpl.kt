package org.craft.packetfactory.packet

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.tree.RootCommandNode
import com.mojang.datafixers.util.Pair
import net.minecraft.server.v1_12_R1.ChatComponentText
import net.minecraft.server.v1_12_R1.PacketPlayOutBed
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity
import net.minecraft.server.v1_16_R3.*
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.TabCompleter
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntMap
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntMaps
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSets
import org.bukkit.craftbukkit.v1_16_R3.CraftStatistic
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeInstance
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeMap
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNBTTagConfigSerializer
import org.bukkit.entity.EntityType
import org.bukkit.util.Consumer
import org.bukkit.util.Vector
import org.craft.packetfactory.MapData
import taboolib.common.platform.function.pluginId
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.chat.ComponentText
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.remap.require
import taboolib.module.nms.saveToString
import java.util.*
import kotlin.jvm.optionals.getOrNull

internal class NMSOutImpl : NMSOut {

    override fun createSpawnEntity(data: PacketData): Any {
        val entityType = data.read<EntityType>("entityType")
        val entityTypeId = entityType.typeId.toInt()
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val location = data.readOrElse("location", emptyLocation)
        val yaw = mathRot(fixYaw(entityType, location.yaw))
        val yHeadRot = data.readOrElse("yHeadRot", yaw.toDouble())

        return PacketPlayOutSpawnEntity().also {
            it.setProperty("a", entityId)
            it.setProperty("b", uuid)
            it.setProperty("c", location.x)
            it.setProperty("d", location.y)
            it.setProperty("e", location.z)
            it.setProperty("i", mathRot(location.pitch))
            it.setProperty("j", yaw)
            it.setProperty("k", entityTypeId)
            it.setProperty("l", yHeadRot)
        }
    }

    override fun createSpawnEntityLiving(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val entityType = data.readOrElse("entityType", EntityType.PLAYER)
        val location = data.readOrElse("location", emptyLocation)
        val yaw = mathRot(fixYaw(entityType, location.yaw))
        val pitch = mathRot(location.pitch)

        return PacketPlayOutSpawnEntityLiving().also {
            it.setProperty("a", entityId)
            it.setProperty("b", uuid)
            it.setProperty("c", entityType.typeId.toInt())
            it.setProperty("d", location.x)
            it.setProperty("e", location.y)
            it.setProperty("f", location.z)
            it.setProperty("j", yaw)
            it.setProperty("k", pitch)
            it.setProperty("l", 0)
            it.setProperty("g", 0)
            it.setProperty("h", 0)
            it.setProperty("i", 0)
            if (MinecraftVersion.versionId < 11600) {
                it.setProperty("n", listOf(_root_ide_package_.net.minecraft.server.v1_9_R2.DataWatcher(null)))
            }
        }

    }

    override fun createSpawnEntityExperienceOrb(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val count = data.readOrElse("count", 0)
        return PacketPlayOutSpawnEntityExperienceOrb().also {
            it.setProperty("a", entityId)
            it.setProperty("b", location.x)
            it.setProperty("c", location.y)
            it.setProperty("d", location.z)
            it.setProperty("e", count)
        }
    }

    override fun createTeleportPosition(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val yaw = mathRot(data.read<Float>("yaw")).toByte()
        return PacketPlayOutEntityTeleport().also {
            it.setProperty("a", entityId)
            it.setProperty("b", yaw)
        }
    }

    override fun createEntityHeadRotation(data: PacketData): Any {
        val entityId = data.readOrElse("entityId", 0)
        return PacketPlayOutEntityHeadRotation().also {
            it.setProperty("a", entityId)
            it.setProperty("b", mathRot(data.read<Float>("yaw")).toByte())
        }
    }

    override fun createEntityMetadata(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val items = data.readOrElse<List<org.bukkit.inventory.ItemStack>>("items", emptyList())
        return PacketPlayOutEntityMetadata().also {
            it.setProperty("a", entityId)
            it.setProperty("b", items)
        }
    }

    override fun createEntityDestroy(data: PacketData): Any {
        val entityIds = data.readOrElse<List<Int>>("entityIds", emptyList())
        return PacketPlayOutEntityDestroy().also {
            it.setProperty("a", entityIds)
        }
    }

    override fun createRelEntityMove(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutRelEntityMove(
            entityId,
            location.blockX.toShort(),
            location.blockY.toShort(),
            location.blockZ.toShort(),
            onGround
        )
    }

    override fun createEntityLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val yaw = mathRot(data.read<Float>("yaw")).toByte()
        val pitch = mathRot(data.read<Float>("pitch")).toByte()
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, yaw, pitch, onGround)
    }

    override fun createRelEntityMoveLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val onGround = data.readOrElse("onGround", false)
        return PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
            entityId,
            location.blockX.toShort(),
            location.blockY.toShort(),
            location.blockZ.toShort(),
            mathRot(location.pitch).toByte(),
            mathRot(location.yaw).toByte(),
            onGround
        )
    }

    override fun createEntityEquipment(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val items = data.readOrElse<List<Map<String, org.bukkit.inventory.ItemStack>>>("items", emptyList()).flatMap { map ->
            map.map { Pair(EnumItemSlot.valueOf(it.key.uppercase()), toNMSItem(it.value)) }
        }
        return PacketPlayOutEntityEquipment(entityId, items)
    }

    override fun createAnimation(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val animation = data.read<Int>("animation")
        return PacketPlayOutAnimation().also {
            it.setProperty("a", entityId)
            it.setProperty("b", animation)
        }
    }

    override fun createAttachEntity(data: PacketData): Any {
        val attackId = data.read<Int>("attackId")
        val entityId = data.read<Int>("entityId")
        return PacketPlayOutAttachEntity().also {
            it.setProperty("a", attackId)
            it.setProperty("b", entityId)
        }
    }

    override fun createEntityVelocity(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val vector = data.read<Vector>("vector")
        return PacketPlayOutEntityVelocity(entityId, Vec3D(vector.x, vector.y, vector.z))
    }

    override fun createBed(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Position>("location")
        return PacketPlayOutBed().also {
            it.setProperty("a", entityId)
            it.setProperty("b", location)
        }
    }

    override fun createClearDialog(data: PacketData): Any {
        unsupported()
    }

    override fun createKeepAlive(data: PacketData): Any {
        val keepAliveID = data.read<Long>("keepAliveID")
        return PacketPlayOutKeepAlive(keepAliveID)
    }

    override fun createPing(data: PacketData): Any {
        unsupported()
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

    /** 暂未实现 */
    override fun createCustomPayload(data: PacketData): Any {
        return PacketPlayOutCustomPayload()
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
        unsupported()
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
        unsupported()
    }

    override fun createLevelChunkWithLight(data: PacketData): Any {
        unsupported()
    }

    override fun createMoveMinecart(data: PacketData): Any {
        unsupported()
    }

    override fun createPlayerChat(data: PacketData): Any {
        val text = component(data.read<String>("text"))
        val type = data.readEnumOrElse(ChatMessageType::class.java, "type", ChatMessageType.SYSTEM)
        val uuid = data.read<UUID>("uuid")
        return PacketPlayOutChat(text, type, uuid)
    }

    override fun createPlayerCombatEnd(data: PacketData): Any {
        val entity = data.readOrElse("entity", -1)
        val duration = data.readOrElse("duration", 0)
        return PacketPlayOutCombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.END_COMBAT)
            it.setProperty("d", duration)
            it.setProperty("c", entity)
        }
    }

    override fun createPlayerCombatEnter(data: PacketData): Any {
        return PacketPlayOutCombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.ENTER_COMBAT)
        }
    }

    override fun createPlayerCombatKill(data: PacketData): Any {
        val attack = data.read<Int>("entityId")
        val entity = data.readOrElse("entity", -1)
        val text = component(data.read<String>("text"))
        return PacketPlayOutCombatEvent().also {
            it.setProperty("a", PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED)
            it.setProperty("b", attack)
            it.setProperty("c", entity)
            it.setProperty("e", text)
        }
    }

    override fun createPlayerInfoRemove(data: PacketData): Any {
        val players = data.read<List<PlayerData>>("players")
        return PacketPlayOutPlayerInfo().also {
            it.setProperty("a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER)
            val b = players.map { p -> it.PlayerInfoData(GameProfile(p.uuid, p.name), p.entityId, EnumGamemode.SURVIVAL, null) }
            it.setProperty("b", b)

        }
    }

    override fun createPlayerInfoUpdate(data: PacketData): Any {
        val type = data.readEnumOrElse(
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction::class.java,
            "type",
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME
        )

        return PacketPlayOutPlayerInfo().also {
            it.setProperty("a", type)
            val b = data.read<List<PlayerData>>("players").map { p ->
                it.PlayerInfoData(GameProfile(p.uuid, p.name), p.entityId, EnumGamemode.SURVIVAL, null)
            }
            it.setProperty("b", b)
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
        val text = component(data.read<String>("text"))
        val uuid = data.read<UUID>("uuid")
        return PacketPlayOutChat(text, ChatMessageType.GAME_INFO, uuid)
    }

    override fun createSetBorderCenter(data: PacketData): Any {
        unsupported()
    }

    override fun createSetBorderLerpSize(data: PacketData): Any {
        unsupported()
    }

    override fun createSetBorderSize(data: PacketData): Any {
        val action = data.readEnum(PacketPlayOutWorldBorder.EnumWorldBorderAction::class.java, "action")
        val scale = data.readOrElse("scale", 1.0)
        val x = data.read<Double>("x")
        val z = data.read<Double>("z")
        val size = data.read<Double>("size")
        val distance = data.readOrElse("distance", 10)
        val time = data.readOrElse("time", 0)
        return PacketPlayOutWorldBorder().also {
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

    override fun createSetBorderWarningDelay(data: PacketData): Any {
        unsupported()
    }

    override fun createSetBorderWarningDistance(data: PacketData): Any {
        unsupported()
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
        unsupported()
    }

    override fun createSetTitlesAnimation(data: PacketData): Any {
        unsupported()
    }

    override fun createSetTitleText(data: PacketData): Any {
        unsupported()
    }

    override fun createStartConfiguration(data: PacketData): Any {
        unsupported()
    }

    override fun createSystemChat(data: PacketData): Any {
        val text = component(data.read<String>("text"))
        val uuid = data.read<UUID>("uuid")
        return PacketPlayOutChat(text, ChatMessageType.SYSTEM, uuid)
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
        val abilities = PlayerAbilities()
        data.readNotNull<Boolean>("isFlying") {
            abilities.isFlying = it
        }
        data.readNotNull<Boolean>("canFly") {
            abilities.canFly = it
        }
        data.readNotNull<Boolean>("isInvulnerable") {
            abilities.isInvulnerable = it
        }
        data.readNotNull<Boolean>("mayBuild") {
            abilities.mayBuild = it
        }
        return PacketPlayOutAbilities(abilities)
    }

    /** 暂未实现 */
    override fun createAdvancements(data: PacketData): Any {
        return PacketPlayOutAdvancements()
    }

    /** 暂未实现 */
    override fun createAutoRecipe(data: PacketData): Any {
        val id = data.read<Int>("id")
        return PacketPlayOutAutoRecipe()
    }

    override fun createBlockAction(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val block = IRegistry.BLOCK[MinecraftKey(data.read<String>("block"))]
        val action = data.readOrElse("action", 0)
        val param = data.readOrElse("param", 0)
        return PacketPlayOutBlockAction(location, block, action, param)
    }

    override fun createBlockBreakAnimation(data: PacketData): Any {
        val id = data.read<Int>("id")
        val location = data.read<Location>("location").toPosition()
        val progress = data.readOrElse("progress", 0)
        return PacketPlayOutBlockBreakAnimation(id, location, progress)
    }

    override fun createBlockChange(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val block = (Blocks::class.java.getField(data.read<String>("block")).get(null) as Block).blockData
        return PacketPlayOutBlockChange(location, block)
    }

    override fun createBossBar(data: PacketData): Any {
        val action = data.readEnum(PacketPlayOutBoss.Action::class.java, "action")
        val text = component(data.read<String>("text"))
        val boss = BossBattleCustom(MinecraftKey(pluginId + "_custom_bossbar"), text)
        boss.a(data.read<UUID>("uuid"))
        boss.a(data.readEnumOrElse(BossBattle.BarColor::class.java, "color", BossBattle.BarColor.WHITE))
        boss.a(data.readOrElse("progress", 0.0f))
        boss.a(data.readEnumOrElse(BossBattle.BarStyle::class.java, "style", BossBattle.BarStyle.PROGRESS))
        boss.a(data.readOrElse("isDarkenSky", false))
        boss.b(data.readOrElse("isPlayMusic", false))
        boss.c(data.readOrElse("isCreateFog", false))
        return PacketPlayOutBoss(action, boss)
    }

    override fun createCamera(data: PacketData): Any {
        return PacketPlayOutCamera().also {
            it.setProperty("a", data.read<Int>("entityId"))
        }
    }

    override fun createCloseWindow(data: PacketData): Any {
        return PacketPlayOutCloseWindow(data.read<Int>("windowId"))
    }

    override fun createCollect(data: PacketData): Any {
        val itemId = data.read<Int>("itemId")
        val playerId = data.read<Int>("playerId")
        val amount = data.read<Int>("amount")
        return PacketPlayOutCollect(itemId, playerId, amount)
    }

    /** 咱不实现 */
    override fun createCommands(data: PacketData): Any {
        val a: RootCommandNode<ICompletionProvider>
        return PacketPlayOutCommands()
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
        return PacketPlayOutEntityEffect(entityId, mobEffect)
    }

    override fun createEntitySound(data: PacketData): Any {
        val soundEffect = SoundEffect(MinecraftKey(pluginId + "_sound"))
        val soundCategory = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val entityId = data.read<Int>("entityId")
        val volume = data.readOrElse("volume", 1.0f)
        val pitch = data.readOrElse("pitch", 1.0f)
        return PacketPlayOutEntitySound().also {
            it.setProperty("a", soundEffect)
            it.setProperty("b", soundCategory)
            it.setProperty("c", entityId)
            it.setProperty("d", volume)
            it.setProperty("e", pitch)
        }
    }

    override fun createEntityStatus(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val status = data.read<Byte>("status")
        return PacketPlayOutEntityStatus().also {
            it.setProperty("a", entityId)
            it.setProperty("b", status)
        }
    }

    override fun createExplosion(data: PacketData): Any {
        val location = data.read<Location>("location")
        val strength = data.readOrElse("strength", 1.0f)
        val locations = data.readOrElse("locations", emptyList<Location>()).map { it.toPosition() }
        val motion = data.readOrElse("motion", Vector())
        return PacketPlayOutExplosion(location.x, location.y, location.z, strength, locations, Vec3D(motion.x, motion.y, motion.z))
    }

    override fun createGameStateChange(data: PacketData): Any {
        val change = PacketPlayOutGameStateChange.a(data.read<Int>("state"))
        val param = data.readOrElse("param", 0.0f)
        return PacketPlayOutGameStateChange(change, param)
    }

    override fun createHeldItemSlot(data: PacketData): Any {
        val slot = data.read<Int>("slot")
        check(slot !in 0..8) { "数值不正确,确保在0~8以内" }
        return PacketPlayOutHeldItemSlot(slot)
    }

    /** 暂不实现 */
    override fun createLightUpdate(data: PacketData): Any {
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        val trustEdges = data.readOrElse("trustEdges", false)
        return PacketPlayOutLightUpdate().also {
            it.setProperty("a", x)
            it.setProperty("b", z)
            it.setProperty("i", trustEdges)
        }
    }

    override fun createLogin(data: PacketData): Any {
        TODO("该包目前不知道什么作用,参数过长暂时不做")
    }

    override fun createLookAt(data: PacketData): Any {
        val anchor = data.readEnumOrElse(ArgumentAnchor.Anchor::class.java, "anchor", ArgumentAnchor.Anchor.EYES)
        val location = data.read<Location>("location")
        return PacketPlayOutLookAt().also {
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
            MapIcon(data.readEnumOrElse(MapIcon.Type::class.java, "type", MapIcon.Type.PLAYER), it.x, it.z, it.rotation, component(it.name))
        }
        val startX = data.readOrElse("startX", 0)
        val startZ = data.readOrElse("startZ", 0)
        val width = data.readOrElse("width", 0)
        val height = data.readOrElse("height", 0)
        return PacketPlayOutMap(mapId, scale, track, locked, maps, colors, startX, startZ, width, height)
    }

    override fun createMount(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val passengers = data.readOrElse("passengers", emptyList<Int>()).toTypedArray()

        return PacketPlayOutMount().also {
            it.setProperty("a", entityId)
            it.setProperty("b", passengers)
        }
    }

    /** 不明白作用,暂不做实现 */
    override fun createMultiBlockChange(data: PacketData): Any {
        val location = data.read<Location>("location")
        val position = SectionPosition.a(location.toPosition())
        val short = ShortSets.EMPTY_SET
        val flag = data.readOrElse("flag", false)
        return PacketPlayOutMultiBlockChange(position, short, ChunkSection(0), flag)
    }

    override fun createNamedSoundEffect(data: PacketData): Any {
        val soundName = data.read<String>("soundName")
        val location = data.read<Location>("location")
        val category = data.readEnumOrElse(SoundCategory::class.java, "category", SoundCategory.PLAYERS)
        val volume = data.readOrElse("volume", 1.0f)
        val pitch = data.readOrElse("pitch", 1.0f)
        return PacketPlayOutNamedSoundEffect(
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
        unsupported()
    }

    override fun createOpenBook(data: PacketData): Any {
        val hand = data.readEnumOrElse(EnumHand::class.java, "hand", EnumHand.MAIN_HAND)
        return PacketPlayOutOpenBook(hand)
    }

    override fun createOpenSignEditor(data: PacketData): Any {
        val location = data.read<Location>("location")
        return PacketPlayOutOpenSignEditor(location.toPosition())
    }

    override fun createOpenWindow(data: PacketData): Any {
        val containerId = data.readOrElse("containerId", 0)
        val type = data.readOrElse("type", "")
        val title = component(data.readOrElse("title", ""))

        val clazz = PacketPlayOutOpenWindow::class.java
        return if (MinecraftVersion.versionId >= 11900) {
            PacketPlayOutOpenWindow(
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
        return PacketPlayOutOpenWindowHorse(windowId, slot, entityId)
    }

    override fun createOpenWindowMerchant(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createPlayerListHeaderFooter(data: PacketData): Any {
        val header = component(data.readOrElse("header", ""))
        val footer = component(data.readOrElse("footer", ""))
        return PacketPlayOutPlayerListHeaderFooter().also {
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
        return PacketPlayOutPosition(
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
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
        return PacketPlayOutRemoveEntityEffect(entityId, MobEffectList.fromId(effectId))
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
        return PacketPlayOutRespawn(dimension, type, seed, previous, gamemode, isDebug, isFlat, isCopy)
    }

    override fun createScoreboardDisplayObjective(data: PacketData): Any {
        val objectiveName = data.read<String>("name")
        val position = data.readOrElse("position", 0)
        val criteria = data.readOrNull<String>("criteria")

        return if (version >= 11300) {
            val healthDisplay = data.readEnumOrElse(NMS16ScoreboardHealthDisplay::class.java, "healthDisplay", NMS16ScoreboardHealthDisplay.INTEGER)
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
        val healthDisplay = data.readEnumOrElse(NMS16ScoreboardHealthDisplay::class.java, "healthDisplay", NMS16ScoreboardHealthDisplay.INTEGER)
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
        val action = data.readEnumOrElse(ScoreboardServer.Action::class.java, "action", ScoreboardServer.Action.CHANGE)
        val objectiveName = data.read<String>("objectiveName")
        val score = data.readOrElse("score", 0)
        val name = data.read<String>("name")
        return PacketPlayOutScoreboardScore(action, objectiveName, name, score)
    }

    override fun createScoreboardTeam(data: PacketData): Any {
        val uniqueName = data.read<String>("name")
        val team = ScoreboardTeam(Scoreboard(), uniqueName)
        data.readNotNull<String>("prefix") {
            team.prefix = component(it)
        }
        data.readNotNull<String>("suffix") {
            team.suffix = component(it)
        }
        val mode = data.read<Int>("mode")
        return PacketPlayOutScoreboardTeam(team, mode)
    }

    override fun createSelectAdvancementTab(data: PacketData): Any {
        val advancement = data.read<String>("advancement")
        return PacketPlayOutSelectAdvancementTab(MinecraftKey(advancement))
    }

    override fun createServerDifficulty(data: PacketData): Any {
        val difficulty = data.readEnumOrElse(EnumDifficulty::class.java, "difficulty", EnumDifficulty.PEACEFUL)
        val isLocked = data.readOrElse("locked", false)
        return PacketPlayOutServerDifficulty(difficulty, isLocked)
    }

    override fun createSetCooldown(data: PacketData): Any {
        val item = data.read<org.bukkit.inventory.ItemStack>("item")
        val cooldown = data.read<Int>("cooldown")
        return PacketPlayOutSetCooldown(toNMSItem(item).item, cooldown)
    }

    override fun createSetSlot(data: PacketData): Any {
        val containerId = data.readOrElse("containerId", 0)
        val slot = data.readOrElse("slot", 0)
        val item = toNMSItem(data.readOrElse("item", org.bukkit.inventory.ItemStack(Material.AIR)))
        return PacketPlayOutSetSlot(containerId, slot, item)
    }

    override fun createSpawnPosition(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val id = data.readOrElse("id", 0f)
        return PacketPlayOutSpawnPosition(location, id)
    }

    override fun createStatistic(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createStopSound(data: PacketData): Any {
        val key = MinecraftKey(data.read<String>("key"))
        val sound = data.readEnumOrElse(SoundCategory::class.java, "sound", SoundCategory.PLAYERS)
        return PacketPlayOutStopSound(key, sound)
    }

    override fun createOutTabComplete(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
        val command = data.read<String>("command")
        val suggestions = data.read<List<TabCompleter>>("suggestion")
        Suggestions.create(command, listOf<Suggestion>())
        return PacketPlayOutTabComplete()
    }

    override fun createTileEntityData(data: PacketData): Any {
        val location = data.read<Location>("location").toPosition()
        val action = data.read<Int>("action")
        val nbt = data.read<ItemTagData>("nbt").saveToString()
        return PacketPlayOutTileEntityData(location, action, MojangsonParser.parse(nbt))
    }

    override fun createUnloadChunk(data: PacketData): Any {
        val x = data.readOrElse("x", 0)
        val z = data.readOrElse("z", 0)
        return PacketPlayOutUnloadChunk(x, z)
    }

    override fun createUpdateAttributes(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val attributes = data.read<List<Attribute>>("attributes").map {
            AttributeModifiable(CraftAttributeMap.toMinecraft(it)) {}
        }
        return PacketPlayOutUpdateAttributes(entityId, attributes)
    }

    override fun createUpdateHealth(data: PacketData): Any {
        val food = data.read<Int>("food")
        val health = data.read<Float>("health")
        val foodSaturation = data.read<Float>("foodSaturation")
        check(foodSaturation in 0.0..5.0) { "饱和度必须在0~5之间" }
        return PacketPlayOutUpdateHealth(health, food, foodSaturation)
    }

    override fun createUpdateTime(data: PacketData): Any {
        val tick = data.read<Long>("tick")
        val time = data.read<Long>("time")
        val isIncreasing = data.readOrElse("increasing", false)
        return PacketPlayOutUpdateTime(tick, time, isIncreasing)
    }

    override fun createVehicleMove(data: PacketData): Any {
        val location = data.read<Location>("location")
        return PacketPlayOutVehicleMove().also {
            it.setProperty("a", location.x)
            it.setProperty("b", location.y)
            it.setProperty("c", location.z)
            it.setProperty("d", location.yaw)
            it.setProperty("e", location.pitch)
        }
    }

    override fun createViewCentre(data: PacketData): Any {
        throw UnsupportedOperationException("暂未实现操作")
    }

    override fun createViewDistance(data: PacketData): Any {
        val distance = data.read<Int>("distance")
        return PacketPlayOutViewDistance(distance)
    }

    override fun createWindowData(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val id = data.read<Int>("id")
        val value = data.read<Int>("value")
        return PacketPlayOutWindowData(containerId, id, value)
    }

    override fun createWindowItems(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val items = NonNullList.a<ItemStack>()
        data.readOrElse("items", emptyList<org.bukkit.inventory.ItemStack>()).forEach { i ->
            items.add(toNMSItem(i))
        }
        return PacketPlayOutWindowItems(containerId, items)
    }

    override fun createWorldEvent(data: PacketData): Any {
        val type = data.read<Int>("type")
        val location = data.readOrElse("location", emptyLocation)
        val dataValue = data.readOrElse("dataValue", 0)
        val flag = data.readOrElse("flag", false)
        return PacketPlayOutWorldEvent(type, location.toPosition(), dataValue, flag)
    }

    override fun createWorldParticles(data: PacketData): Any {
        PacketPlayOutWorldParticles()
        TODO("Not yet implemented")
    }

    override fun createNamedEntitySpawn(data: PacketData): Any {
        val id = data.read<Int>("id")
        val uuid = data.read<UUID>("uuid")
        val location = data.readOrElse("location", emptyLocation)
        return PacketPlayOutNamedEntitySpawn().also {
            it.setProperty("a", id)
            it.setProperty("b", uuid)
            it.setProperty("c", location.x)
            it.setProperty("d", location.y)
            it.setProperty("e", location.z)
            it.setProperty("f", location.yaw)
            it.setProperty("g", location.pitch)
        }
    }

    override fun createSpawnEntityPainting(data: PacketData): Any {
        val type = data.read<String>("type")
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val motive = try {
            IRegistry.MOTIVE.a(Paintings::class.java.getField(type).get(null) as? Paintings)
        } catch (_: NoSuchFieldError) {
            type
        }
        val direction = data.readEnumOrElse(EnumDirection::class.java, "direction", EnumDirection.NORTH)
        val location = data.readOrElse("location", emptyLocation)
        return PacketPlayOutSpawnEntityPainting().also {
            it.setProperty("a", entityId)
            it.setProperty("b", uuid)
            it.setProperty("c", location.toPosition())
            it.setProperty("d", direction)
            it.setProperty("e", motive)
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

    fun getDataWatcher(value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.a), value)
    }

    fun getDataWatcher(value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.b), value)
    }

    fun getDataWatcher(value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.c), value)
    }

    fun getDataWatcher(value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.d), value)
    }

    fun getDataWatcher(value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.e), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(value: Optional<*>): DataWatcher.Item<out Optional<out Any?>?> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.f), value as Optional<IChatBaseComponent>)

            is IBlockData -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.h), value as Optional<IBlockData>)

            is BlockPosition -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.m), value as Optional<BlockPosition>)

            is UUID -> DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.o), value as Optional<UUID>)

            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcher(value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.g), value)
    }

    fun getDataWatcher(value: Boolean): DataWatcher.Item<Boolean> {
        return if (MinecraftVersion.versionId >= 11605) {
            DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.i), value)
        } else {
            DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry::class.java.getProperty<DataWatcherSerializer<Boolean>>("h", true)!!), value)
        }
    }

    fun getDataWatcher(value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.j), value)
    }

    fun getDataWatcher(value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.k), value)
    }

    fun getDataWatcher(value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.l), value)
    }

    fun getDataWatcher(value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.n), value)
    }

    fun getDataWatcher(value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.p), value)
    }

    fun getDataWatcher(value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.q), value)
    }

    fun getDataWatcher(value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.r), value)
    }

    fun getDataWatcher(value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.s), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getDataWatcher(value: Any): DataWatcher.Item<*> {
        if (value is DataWatcher.Item<*>) return value
        if (require(EntityPose::class.java) && value is EntityPose) {
            return getDataWatcher(value)
        }
        if (require(VillagerData::class.java) && value is VillagerData) {
            return getDataWatcher(value)
        }
        if (require(ParticleParam::class.java) && value is ParticleParam) {
            return getDataWatcher(value)
        }
        return when (value) {
            is String -> getDataWatcher(value)
            is Byte -> getDataWatcher(value)
            is Float -> getDataWatcher(value)
            is NBTTagCompound -> getDataWatcher(value)
            is OptionalInt -> getDataWatcher(value)
            is Optional<*> -> getOptionalDataWatcher(value)
            is EnumDirection -> getDataWatcher(value)
            is BlockPosition -> getDataWatcher(value)
            is Vector3f -> getDataWatcher(value)
            is Boolean -> getDataWatcher(value)
            is IChatBaseComponent -> getDataWatcher(value)
            is ItemStack -> getDataWatcher(value)
            is Int -> getDataWatcher(value)
            is org.bukkit.inventory.ItemStack -> getDataWatcher(toNMSItem(value))
            else -> error("不支持的类型: $value")
        }
    }

}