package org.craft.packetfactory.packet

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.craft.packetfactory.data.PacketData
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.MinecraftVersion

interface NMSOut {

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

    fun mathRot(yaw: Float): Int {
        return (yaw * 256.0f / 360.0f).toInt()
    }

    fun unsupported(): Nothing {
        throw UnsupportedVersionException()
    }

    fun createSpawnEntity(data: PacketData): Any

    fun createSpawnEntityLiving(data: PacketData): Any

    fun createSpawnEntityExperienceOrb(data: PacketData): Any

    fun createTeleportPosition(data: PacketData): Any

    fun createEntityHeadRotation(data: PacketData): Any

    fun createEntityMetadata(data: PacketData): Any

    fun createEntityDestroy(data: PacketData): Any

    fun createRelEntityMove(data: PacketData): Any

    fun createEntityLook(data: PacketData): Any

    fun createRelEntityMoveLook(data: PacketData): Any

    fun createEntityEquipment(data: PacketData): Any

    fun createAnimation(data: PacketData): Any

    fun createAttachEntity(data: PacketData): Any

    fun createEntityVelocity(data: PacketData): Any

    fun createBed(data: PacketData): Any

    fun createClearDialog(data: PacketData): Any

    fun createKeepAlive(data: PacketData): Any

    fun createPing(data: PacketData): Any
    fun createResourcePackPop(data: PacketData): Any
    fun createResourcePackPush(data: PacketData): Any
    fun createServerLinks(data: PacketData): Any
    fun createShowDialog(data: PacketData): Any
    fun createStoreCookie(data: PacketData): Any
    fun createTransfer(data: PacketData): Any
    fun createUpdateTags(data: PacketData): Any
    fun createClientInformation(data: PacketData): Any
    fun createCustomClickAction(data: PacketData): Any
    fun createCustomPayload(data: PacketData): Any
    fun createPong(data: PacketData): Any
    fun createResourcePack(data: PacketData): Any
    fun createCodeOfConduct(data: PacketData): Any
    fun createFinishConfiguration(data: PacketData): Any
    fun createRegistryData(data: PacketData): Any
    fun createResetChat(data: PacketData): Any
    fun createSelectKnown(data: PacketData): Any
    fun createUpdateEnabledFeatures(data: PacketData): Any
    fun createAcceptCodeOfConduct(data: PacketData): Any
    fun createCookieRequest(data: PacketData): Any
    fun createCookieResponse(data: PacketData): Any
    fun createBlockChangedAck(data: PacketData): Any
    fun createBundleDelimiter(data: PacketData): Any
    fun createChunkBatchFinished(data: PacketData): Any
    fun createChunkBatchStart(data: PacketData): Any
    fun createChunksBiomes(data: PacketData): Any
    fun createClearTitles(data: PacketData): Any
    fun createCustomChatCompletions(data: PacketData): Any
    fun createDamageEvent(data: PacketData): Any
    fun createDebugBlockValue(data: PacketData): Any
    fun createDebugChunkValue(data: PacketData): Any
    fun createDebugEntityValue(data: PacketData): Any
    fun createDebugEvent(data: PacketData): Any
    fun createDebugSample(data: PacketData): Any
    fun createDeleteChat(data: PacketData): Any
    fun createDisguisedChat(data: PacketData): Any
    fun createEntityPositionSync(data: PacketData): Any
    fun createGameTestHighlightPos(data: PacketData): Any
    fun createHurtAnimation(data: PacketData): Any
    fun createInitializeBorder(data: PacketData): Any
    fun createLevelChunkWithLight(data: PacketData): Any
    fun createMoveMinecart(data: PacketData): Any
    fun createPlayerChat(data: PacketData): Any
    fun createPlayerCombatEnd(data: PacketData): Any
    fun createPlayerCombatEnter(data: PacketData): Any
    fun createPlayerCombatKill(data: PacketData): Any
    fun createPlayerInfoRemove(data: PacketData): Any
    fun createPlayerInfoUpdate(data: PacketData): Any
    fun createPlayerRotation(data: PacketData): Any
    fun createProjectilePower(data: PacketData): Any
    fun createRecipeBookAdd(data: PacketData): Any
    fun createRecipeBookRemove(data: PacketData): Any
    fun createRecipeBookSettings(data: PacketData): Any
    fun createResetScore(data: PacketData): Any
    fun createServerData(data: PacketData): Any
    fun createSetActionBarText(data: PacketData): Any
    fun createSetBorderCenter(data: PacketData): Any
    fun createSetBorderLerpSize(data: PacketData): Any
    fun createSetBorderSize(data: PacketData): Any
    fun createSetBorderWarningDelay(data: PacketData): Any
    fun createSetBorderWarningDistance(data: PacketData): Any
    fun createSetCursorItem(data: PacketData): Any
    fun createSetPlayerInventory(data: PacketData): Any
    fun createSetSimulationDistance(data: PacketData): Any
    fun createSetSubtitleText(data: PacketData): Any
    fun createSetTitlesAnimation(data: PacketData): Any
    fun createSetTitleText(data: PacketData): Any
    fun createStartConfiguration(data: PacketData): Any
    fun createSystemChat(data: PacketData): Any
    fun createTestInstanceBlockStatus(data: PacketData): Any
    fun createTickingState(data: PacketData): Any
    fun createTickingStep(data: PacketData): Any
    fun createTrackedWaypoint(data: PacketData): Any
    fun createAbilities(data: PacketData): Any
    fun createAdvancements(data: PacketData): Any
    fun createAutoRecipe(data: PacketData): Any
    fun createBlockAction(data: PacketData): Any
    fun createBlockBreakAnimation(data: PacketData): Any
    fun createBlockChange(data: PacketData): Any
    fun createBossBar(data: PacketData): Any
    fun createCamera(data: PacketData): Any
    fun createCloseWindow(data: PacketData): Any
    fun createCollect(data: PacketData): Any
    fun createCommands(data: PacketData): Any
    fun createEntityEffect(data: PacketData): Any
    fun createEntitySound(data: PacketData): Any
    fun createEntityStatus(data: PacketData): Any
    fun createExplosion(data: PacketData): Any
    fun createGameStateChange(data: PacketData): Any
    fun createHeldItemSlot(data: PacketData): Any
    fun createLightUpdate(data: PacketData): Any
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
    fun createSpawnEntityPainting(data: PacketData): Any
}