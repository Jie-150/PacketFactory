package org.craft.packetfactory.packet

import com.mojang.datafixers.util.Pair
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import org.craft.packetfactory.data.PacketData
import taboolib.common.UnsupportedVersionException
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.NMSItemTag
import taboolib.module.nms.createDataSerializer
import taboolib.module.nms.nmsClass
import java.util.*

interface NMSPacket {

    val version: Int
        get() = MinecraftVersion.versionId

    val emptyLocation: Location
        get() = Location(null, 0.0, 0.0, 0.0, 0.0f, 0.0f)

    fun fixYaw(type: EntityType, yaw: Float): Float {
        return when (type.name) {
            "WITHER_SKULL" -> yaw + 180
            "MINECART",
            "CHEST_MINECART",
            "COMMAND_BLOCK_MINECART",
            "FURNACE_MINECART",
            "HOPPER_MINECART",
            "TNT_MINECART",
            "SPAWNER_MINECART" -> yaw + 90

            else -> yaw
        }
    }

    fun mathRot(yaw: Float): Double {
        return yaw * 256.0 / 360.0
    }

    fun unsupported(): Nothing {
        throw UnsupportedVersionException()
    }

    fun createSpawnEntity(data: PacketData): Any
    fun createSpawnEntityLiving(data: PacketData): Any
    fun createSpawnEntityExperienceOrb(data: PacketData): Any {
        val location = data.read<Location>("location")
        return nmsClass("PacketPlayOutSpawnEntityExperienceOrb").invokeConstructor(createDataSerializer {
            writeInt(data.read("entityId"))
            writeDouble(location.x)
            writeDouble(location.y)
            writeDouble(location.z)
            writeShort(data.readOrElse("count", 0.toShort()))
        }.build())
    }

    fun createEntityTeleport(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        val onGround = data.read<Boolean>("onGround")
        return nmsClass("PacketPlayOutEntityTeleport").invokeConstructor(createDataSerializer {
            writeInt(entityId)
            writeDouble(location.x)
            writeDouble(location.y)
            writeDouble(location.z)
            writeFloat(mathRot(location.pitch).toFloat())
            writeFloat(mathRot(location.yaw).toFloat())
            writeBoolean(onGround)
        }.build())
    }

    fun createEntityHeadRotation(data: PacketData): Any {
        return nmsClass("PacketPlayOutEntityHeadRotation").invokeConstructor(createDataSerializer {
            writeInt(data.read("entityId"))
            writeByte(data.read("yHeadRot"))
        }.build())
    }

    fun createEntityMetadata(data: PacketData): Any
    fun createEntityDestroy(data: PacketData): Any {
        val entitys = data.read<List<Int>>("entityIds").toIntArray()
        return nmsClass("PacketPlayOutEntityDestroy").invokeConstructor(entitys)
    }

    fun createRelEntityMove(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.readOrElse("location", emptyLocation)
        val onGround = data.readOrElse("onGround", false)
        return nmsClass("PacketPlayOutEntity\$PacketPlayOutRelEntityMove").invokeConstructor(
            entityId,
            location.blockX.toShort(),
            location.blockY.toShort(),
            location.blockZ.toShort(),
            onGround
        )
    }

    fun createEntityLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val yaw = mathRot(data.read<Byte>("yaw").toFloat())
        val pitch = mathRot(data.read<Byte>("pitch").toFloat())
        val onGround = data.readOrElse("onGround", false)
        return nmsClass("PacketPlayOutEntity\$PacketPlayOutEntityLook").invokeConstructor(entityId, pitch, yaw, onGround)
    }

    fun createRelEntityMoveLook(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val location = data.read<Location>("location")
        val onGround = data.readOrElse("onGround", false)
        return nmsClass("PacketPlayOutEntity\$PacketPlayOutRelEntityMoveLook").invokeConstructor(
            entityId,
            location.x.toInt().toShort(),
            location.y.toInt().toShort(),
            location.z.toInt().toShort(),
            location.pitch.toInt().toByte(),
            location.yaw.toInt().toByte(),
            onGround
        )
    }

    fun createEntityEquipment(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val equipments = data.read<Map<String, org.bukkit.inventory.ItemStack>>("equipments").map {
            Pair(nmsClass("EnumItemSlot").invokeMethod<Any>("valueOf", it.key.uppercase(), isStatic = true), NMSItemTag.asNMSCopy(it.value))
        }
        return nmsClass("PacketPlayOutEntityEquipment").invokeConstructor(entityId, equipments)
    }

    fun createAnimation(data: PacketData): Any {
        return nmsClass("PacketPlayOutAnimation").invokeConstructor(createDataSerializer {
            writeInt(data.read("entityId"))
            writeByte(data.read<Int>("animation").toByte())
        }.build())
    }

    fun createAttachEntity(data: PacketData): Any {
        return nmsClass("PacketPlayOutAttachEntity").invokeConstructor(createDataSerializer {
            writeInt(data.read("sourceId"))
            writeInt(data.read("destId"))
        }.build())
    }

    fun createEntityVelocity(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val vector = data.read<Vector>("vector")
        return nmsClass("PacketPlayOutEntityVelocity").invokeConstructor(entityId, nmsClass("Vec3D").invokeConstructor(vector.x, vector.y, vector.z))
    }

    fun createBed(data: PacketData): Any {
        unsupported()
    }

    fun createKeepAlive(data: PacketData): Any {
        unsupported()
    }

    fun createPing(data: PacketData): Any {
        unsupported()
    }

    fun createResourcePackPop(data: PacketData): Any {
        unsupported()
    }

    fun createResourcePackPush(data: PacketData): Any {
        unsupported()
    }

    fun createServerLinks(data: PacketData): Any {
        unsupported()
    }

    fun createShowDialog(data: PacketData): Any {
        unsupported()
    }

    fun createStoreCookie(data: PacketData): Any {
        unsupported()
    }

    fun createTransfer(data: PacketData): Any {
        unsupported()
    }

    fun createUpdateTags(data: PacketData): Any {
        unsupported()
    }

    fun createClientInformation(data: PacketData): Any {
        unsupported()
    }

    fun createCustomClickAction(data: PacketData): Any {
        unsupported()
    }

    fun createCustomPayload(data: PacketData): Any {
        unsupported()
    }

    fun createPong(data: PacketData): Any {
        unsupported()
    }

    fun createResourcePack(data: PacketData): Any {
        unsupported()
    }

    fun createCodeOfConduct(data: PacketData): Any {
        unsupported()
    }

    fun createFinishConfiguration(data: PacketData): Any {
        unsupported()
    }

    fun createRegistryData(data: PacketData): Any {
        unsupported()
    }

    fun createSelectKnown(data: PacketData): Any {
        unsupported()
    }

    fun createUpdateEnabledFeatures(data: PacketData): Any {
        unsupported()
    }

    fun createAcceptCodeOfConduct(data: PacketData): Any {
        unsupported()
    }

    fun createCookieRequest(data: PacketData): Any {
        unsupported()
    }

    fun createCookieResponse(data: PacketData): Any {
        unsupported()
    }

    fun createBlockChangedAck(data: PacketData): Any {
        unsupported()
    }

    fun createBundleDelimiter(data: PacketData): Any {
        unsupported()
    }

    fun createChunkBatchFinished(data: PacketData): Any {
        unsupported()
    }

    fun createChunkBatchStart(data: PacketData): Any {
        unsupported()
    }

    fun createChunksBiomes(data: PacketData): Any {
        unsupported()
    }

    fun createClearTitles(data: PacketData): Any {
        unsupported()
    }

    fun createCustomChatCompletions(data: PacketData): Any {
        unsupported()
    }

    fun createDamageEvent(data: PacketData): Any {
        unsupported()
    }

    fun createDebugBlockValue(data: PacketData): Any {
        unsupported()
    }

    fun createDebugChunkValue(data: PacketData): Any {
        unsupported()
    }

    fun createDebugEntityValue(data: PacketData): Any {
        unsupported()
    }

    fun createDebugEvent(data: PacketData): Any {
        unsupported()
    }

    fun createDebugSample(data: PacketData): Any {
        unsupported()
    }

    fun createDeleteChat(data: PacketData): Any {
        unsupported()
    }

    fun createDisguisedChat(data: PacketData): Any {
        unsupported()
    }

    fun createEntityPositionSync(data: PacketData): Any {
        unsupported()
    }

    fun createGameTestHighlightPos(data: PacketData): Any {
        unsupported()
    }

    fun createHurtAnimation(data: PacketData): Any {
        unsupported()
    }

    fun createInitializeBorder(data: PacketData): Any {
        unsupported()
    }

    fun createLevelChunkWithLight(data: PacketData): Any {
        unsupported()
    }

    fun createMoveMinecart(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerChat(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerCombatEnd(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerCombatEnter(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerCombatKill(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerInfoRemove(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerInfoUpdate(data: PacketData): Any {
        unsupported()
    }

    fun createPlayerRotation(data: PacketData): Any {
        unsupported()
    }

    fun createProjectilePower(data: PacketData): Any {
        unsupported()
    }

    fun createRecipeBookAdd(data: PacketData): Any {
        unsupported()
    }

    fun createRecipeBookRemove(data: PacketData): Any {
        unsupported()
    }

    fun createRecipeBookSettings(data: PacketData): Any {
        unsupported()
    }

    fun createResetScore(data: PacketData): Any {
        unsupported()
    }

    fun createServerData(data: PacketData): Any {
        unsupported()
    }

    fun createSetActionBarText(data: PacketData): Any {
        unsupported()
    }

    fun createSetBorderCenter(data: PacketData): Any {
        unsupported()
    }

    fun createSetBorderLerpSize(data: PacketData): Any {
        unsupported()
    }

    fun createSetBorderSize(data: PacketData): Any {
        unsupported()
    }

    fun createSystemChat(data: PacketData): Any
    fun createTestInstanceBlockStatus(data: PacketData): Any {
        unsupported()
    }

    fun createTickingState(data: PacketData): Any {
        unsupported()
    }

    fun createTickingStep(data: PacketData): Any {
        unsupported()
    }

    fun createTrackedWaypoint(data: PacketData): Any {
        unsupported()
    }

    fun createAbilities(data: PacketData): Any

    fun createSetBorderWarningDelay(data: PacketData): Any {
        unsupported()
    }

    fun createSetBorderWarningDistance(data: PacketData): Any {
        unsupported()
    }

    fun createSetCursorItem(data: PacketData): Any {
        unsupported()
    }

    fun createSetPlayerInventory(data: PacketData): Any {
        unsupported()
    }

    fun createSetSimulationDistance(data: PacketData): Any {
        unsupported()
    }

    fun createSetSubtitleText(data: PacketData): Any {
        unsupported()
    }

    fun createSetTitlesAnimation(data: PacketData): Any {
        unsupported()
    }

    fun createSetTitleText(data: PacketData): Any {
        unsupported()
    }

    fun createStartConfiguration(data: PacketData): Any {
        unsupported()
    }

    fun createAdvancements(data: PacketData): Any {
        unsupported()
    }

    fun createAutoRecipe(data: PacketData): Any {
        unsupported()
    }

    fun createBlockAction(data: PacketData): Any {
        unsupported()
    }

    fun createBlockBreakAnimation(data: PacketData): Any {
        unsupported()
    }

    fun createBlockChange(data: PacketData): Any {
        unsupported()
    }

    fun createBossBar(data: PacketData): Any {
        unsupported()
    }

    fun createCamera(data: PacketData): Any {
        return nmsClass("PacketPlayOutCamera").invokeConstructor(createDataSerializer {
            writeInt(data.read("cameraId"))
        }.build())
    }

    fun createCloseWindow(data: PacketData): Any {
        return nmsClass("PacketPlayOutCloseWindow").invokeConstructor(data.read<Int>("containerId"))
    }

    fun createCollect(data: PacketData): Any {
        val itemId = data.read<Int>("itemId")
        val playerId = data.read<Int>("playerId")
        val amount = data.read<Int>("amount")
        return nmsClass("PacketPlayOutCollect").invokeConstructor(itemId, playerId, amount)
    }

    fun createCommands(data: PacketData): Any
    fun createEntityEffect(data: PacketData): Any
    fun createEntitySound(data: PacketData): Any
    fun createEntityStatus(data: PacketData): Any {
        return nmsClass("PacketPlayOutEntityStatus").invokeConstructor(createDataSerializer {
            writeInt(data.read("entityId"))
            writeByte(data.read("status"))
        }.build())
    }

    fun createExplosion(data: PacketData): Any {
        unsupported()
    }

    fun createGameStateChange(data: PacketData): Any
    fun createHeldItemSlot(data: PacketData): Any {
        val slot = data.read<Int>("slot")
        check(slot in 0..8) { "数值不正确,确保在0~8以内" }
        return nmsClass("PacketPlayOutHeldItemSlot").invokeConstructor(slot)
    }

    fun createLightUpdate(data: PacketData): Any {
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        val trustEdges = data.read<Boolean>("trustEdges")
        val skyYMask = data.read<BitSet>("skyYMask")
        val blockYMask = data.read<BitSet>("blockYMask")
        val emptySkyYMask = data.readOrElse("emptySkyYMask", BitSet())
        val emptyBlockYMask = data.readOrElse("emptyBlockYMask", BitSet())
        val skyUpdates = data.readOrElse("skyUpdates", listOf<ByteArray>())
        val blockUpdates = data.readOrElse<List<ByteArray>>("blockUpdates", listOf())
        return nmsClass("PacketPlayOutLightUpdate").invokeConstructor(createDataSerializer {
            writeInt(x)
            writeInt(z)
            writeBoolean(trustEdges)
            // skyYMask
            writeVarInt(skyYMask.toLongArray().size)
            skyYMask.toLongArray().forEach(::writeLong)
            // blockYMask
            writeVarInt(blockYMask.toLongArray().size)
            blockYMask.toLongArray().forEach(::writeLong)
            // emptySkyYMask
            writeVarInt(emptySkyYMask.toLongArray().size)
            emptySkyYMask.toLongArray().forEach(::writeLong)
            // emptyBlockYMask
            writeVarInt(emptyBlockYMask.toLongArray().size)
            emptyBlockYMask.toLongArray().forEach(::writeLong)
            // skyUpdates
            writeVarInt(skyUpdates.size)
            skyUpdates.forEach { bytes ->
                writeVarInt(2048)
                writeBytes(bytes)
            }
            // blockUpdates
            writeVarInt(blockUpdates.size)
            blockUpdates.forEach { bytes ->
                writeVarInt(2048)
                writeBytes(bytes)
            }
        }.build())
    }

    fun createLogin(data: PacketData): Any
    fun createLookAt(data: PacketData): Any
    fun createMap(data: PacketData): Any
    fun createMount(data: PacketData): Any
    fun createMultiBlockChange(data: PacketData): Any
    fun createNamedSoundEffect(data: PacketData): Any
    fun createNBTQuery(data: PacketData): Any
    fun createOpenBook(data: PacketData): Any
    fun createOpenSignEditor(data: PacketData): Any
    fun createOpenWindow(data: PacketData): Any
    fun createOpenWindowHorse(data: PacketData): Any
    fun createOpenWindowMerchant(data: PacketData): Any
    fun createPlayerListHeaderFooter(data: PacketData): Any
    fun createPosition(data: PacketData): Any
    fun createRecipeUpdate(data: PacketData): Any
    fun createRemoveEntityEffect(data: PacketData): Any
    fun createRespawn(data: PacketData): Any
    fun createScoreboardDisplayObjective(data: PacketData): Any
    fun createScoreboardObjective(data: PacketData): Any
    fun createScoreboardScore(data: PacketData): Any
    fun createScoreboardTeam(data: PacketData): Any
    fun createSelectAdvancementTab(data: PacketData): Any
    fun createServerDifficulty(data: PacketData): Any
    fun createSetCooldown(data: PacketData): Any
    fun createSetSlot(data: PacketData): Any
    fun createSpawnPosition(data: PacketData): Any
    fun createStatistic(data: PacketData): Any
    fun createStopSound(data: PacketData): Any
    fun createOutTabComplete(data: PacketData): Any
    fun createTileEntityData(data: PacketData): Any
    fun createUnloadChunk(data: PacketData): Any
    fun createUpdateAttributes(data: PacketData): Any
    fun createUpdateHealth(data: PacketData): Any
    fun createUpdateTime(data: PacketData): Any
    fun createVehicleMove(data: PacketData): Any
    fun createViewCentre(data: PacketData): Any
    fun createViewDistance(data: PacketData): Any
    fun createWindowData(data: PacketData): Any
    fun createWindowItems(data: PacketData): Any
    fun createWorldEvent(data: PacketData): Any
    fun createWorldParticles(data: PacketData): Any
    fun createNamedEntitySpawn(data: PacketData): Any
    fun createSpawnEntityPainting(data: PacketData): Any {
        TODO("1.19+ 不再支持发Painting包")
    }
}