package org.craft.packetfactory.packet

import net.minecraft.core.*
import net.minecraft.core.particles.ParticleParam
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.DataWatcher
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.resources.MinecraftKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityPose
import net.minecraft.world.entity.npc.VillagerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.IBlockData
import net.minecraft.world.phys.Vec3D
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.remap.require
import java.util.*
import kotlin.jvm.optionals.getOrNull

internal class NMS17 : NMSOut {
    override fun createSpawnEntity(data: PacketData): Any {
        val entityType = data.read<String>("entityType")
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")

        val location = data.read<Location>("location")
        val data = data.readOrElse("data", 0)
        val type = IRegistry.ENTITY_TYPE.get(MinecraftKey(entityType.uppercase()))
        return PacketPlayOutSpawnEntity(
            entityId,
            uuid,
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            type,
            data,
            Vec3D.ZERO
        )
    }

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
        TODO("PacketPlayOutEntityTeleport: 实体传送位置数据包")
    }

    override fun createEntityHeadRotation(data: PacketData): Any {
        TODO("PacketPlayOutEntityHeadRotation: 实体头部旋转数据包")
    }

    override fun createEntityMetadata(data: PacketData): Any {
        TODO("PacketPlayOutEntityMetadata: 实体元数据更新数据包")
    }

    override fun createEntityDestroy(data: PacketData): Any {
        TODO("PacketPlayOutEntityDestroy: 实体销毁数据包")
    }

    override fun createRelEntityMove(data: PacketData): Any {
        TODO("PacketPlayOutRelEntityMove: 实体相对移动数据包")
    }

    override fun createEntityLook(data: PacketData): Any {
        TODO("PacketPlayOutEntityLook: 实体朝向数据包")
    }

    override fun createRelEntityMoveLook(data: PacketData): Any {
        TODO("PacketPlayOutRelEntityMoveLook: 实体移动和朝向数据包")
    }

    override fun createEntityEquipment(data: PacketData): Any {
        TODO("PacketPlayOutEntityEquipment: 实体装备数据包")
    }

    override fun createAnimation(data: PacketData): Any {
        TODO("PacketPlayOutAnimation: 实体动画数据包")
    }

    override fun createAttachEntity(data: PacketData): Any {
        TODO("PacketPlayOutAttachEntity: 实体附着数据包")
    }

    override fun createEntityVelocity(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBed(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createClearDialog(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createKeepAlive(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPing(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createResourcePackPop(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createResourcePackPush(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createServerLinks(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createShowDialog(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createStoreCookie(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTransfer(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUpdateTags(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createClientInformation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCustomClickAction(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCustomPayload(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPong(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createResourcePack(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCodeOfConduct(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createFinishConfiguration(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRegistryData(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createResetChat(data: PacketData): Any {
        TODO("PacketPlayOutResetChat: 重置聊天数据包")
    }

    override fun createSelectKnown(data: PacketData): Any {
        TODO("PacketPlayOutSelectKnown: 选择已知数据包")
    }

    override fun createUpdateEnabledFeatures(data: PacketData): Any {
        TODO("PacketPlayOutUpdateEnabledFeatures: 更新启用功能数据包")
    }

    override fun createAcceptCodeOfConduct(data: PacketData): Any {
        TODO("PacketPlayOutAcceptCodeOfConduct: 接受行为准则数据包")
    }

    override fun createCookieRequest(data: PacketData): Any {
        TODO("PacketPlayOutCookieRequest: Cookie请求数据包")
    }

    override fun createCookieResponse(data: PacketData): Any {
        TODO("PacketPlayOutCookieResponse: Cookie响应数据包")
    }

    override fun createBlockChangedAck(data: PacketData): Any {
        TODO("PacketPlayOutBlockChangedAck: 方块变更确认数据包")
    }

    override fun createBundleDelimiter(data: PacketData): Any {
        TODO("PacketPlayOutBundleDelimiter: 数据包束分隔符数据包")
    }

    override fun createChunkBatchFinished(data: PacketData): Any {
        TODO("PacketPlayOutChunkBatchFinished: 区块批次完成数据包")
    }

    override fun createChunkBatchStart(data: PacketData): Any {
        TODO("PacketPlayOutChunkBatchStart: 区块批次开始数据包")
    }

    override fun createChunksBiomes(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createClearTitles(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCustomChatCompletions(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDamageEvent(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDebugBlockValue(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDebugChunkValue(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDebugEntityValue(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDebugEvent(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDebugSample(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDeleteChat(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createDisguisedChat(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityPositionSync(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createGameTestHighlightPos(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createHurtAnimation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createInitializeBorder(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createLevelChunkWithLight(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createMoveMinecart(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerChat(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerCombatEnd(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerCombatEnter(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerCombatKill(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerInfoRemove(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerInfoUpdate(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerRotation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createProjectilePower(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRecipeBookAdd(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRecipeBookRemove(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRecipeBookSettings(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createResetScore(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createServerData(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetActionBarText(data: PacketData): Any {
        TODO("PacketPlayOutSetActionBarText: 设置操作栏文本数据包")
    }

    override fun createSetBorderCenter(data: PacketData): Any {
        TODO("PacketPlayOutSetBorderCenter: 设置边界中心数据包")
    }

    override fun createSetBorderLerpSize(data: PacketData): Any {
        TODO("PacketPlayOutSetBorderLerpSize: 设置边界渐变大小数据包")
    }

    override fun createSetBorderSize(data: PacketData): Any {
        TODO("PacketPlayOutSetBorderSize: 设置边界大小数据包")
    }

    override fun createSetBorderWarningDelay(data: PacketData): Any {
        TODO("PacketPlayOutSetBorderWarningDelay: 设置边界警告延迟数据包")
    }

    override fun createSetBorderWarningDistance(data: PacketData): Any {
        TODO("PacketPlayOutSetBorderWarningDistance: 设置边界警告距离数据包")
    }

    override fun createSetCursorItem(data: PacketData): Any {
        TODO("PacketPlayOutSetCursorItem: 设置光标物品数据包")
    }

    override fun createSetPlayerInventory(data: PacketData): Any {
        TODO("PacketPlayOutSetPlayerInventory: 设置玩家物品栏数据包")
    }

    override fun createSetSimulationDistance(data: PacketData): Any {
        TODO("PacketPlayOutSetSimulationDistance: 设置模拟距离数据包")
    }

    override fun createSetSubtitleText(data: PacketData): Any {
        TODO("PacketPlayOutSetSubtitleText: 设置副标题文本数据包")
    }

    override fun createSetTitlesAnimation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetTitleText(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createStartConfiguration(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSystemChat(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTestInstanceBlockStatus(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTickingState(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTickingStep(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTrackedWaypoint(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAbilities(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAdvancements(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAutoRecipe(data: PacketData): Any {
        TODO("PacketPlayOutAutoRecipe: 自动配方数据包")
    }

    override fun createBlockAction(data: PacketData): Any {
        TODO("PacketPlayOutBlockAction: 方块动作数据包")
    }

    override fun createBlockBreakAnimation(data: PacketData): Any {
        TODO("PacketPlayOutBlockBreakAnimation: 方块破坏动画数据包")
    }

    override fun createBlockChange(data: PacketData): Any {
        TODO("PacketPlayOutBlockChange: 方块变更数据包")
    }

    override fun createBossBar(data: PacketData): Any {
        TODO("PacketPlayOutBossBar: Boss血条数据包")
    }

    override fun createCamera(data: PacketData): Any {
        TODO("PacketPlayOutCamera: 摄像机数据包")
    }

    override fun createCloseWindow(data: PacketData): Any {
        TODO("PacketPlayOutCloseWindow: 关闭窗口数据包")
    }

    override fun createCollect(data: PacketData): Any {
        TODO("PacketPlayOutCollect: 收集物品数据包")
    }

    override fun createCommands(data: PacketData): Any {
        TODO("PacketPlayOutCommands: 命令数据包")
    }

    override fun createEntityEffect(data: PacketData): Any {
        TODO("PacketPlayOutEntityEffect: 实体效果数据包")
    }

    override fun createEntitySound(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityStatus(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createExplosion(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createGameStateChange(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createHeldItemSlot(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createLightUpdate(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createLogin(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createLookAt(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createMap(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createMount(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createMultiBlockChange(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createNamedSoundEffect(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createNBTQuery(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenBook(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenSignEditor(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenWindow(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenWindowHorse(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOpenWindowMerchant(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPlayerListHeaderFooter(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createPosition(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRecipeUpdate(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRemoveEntityEffect(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRespawn(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createScoreboardDisplayObjective(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createScoreboardObjective(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createScoreboardScore(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createScoreboardTeam(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSelectAdvancementTab(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createServerDifficulty(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetCooldown(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetSlot(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSpawnPosition(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createStatistic(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createStopSound(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createOutTabComplete(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createTileEntityData(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUnloadChunk(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUpdateAttributes(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUpdateHealth(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUpdateTime(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createVehicleMove(data: PacketData): Any {
        val location = data.read<Location>("location")
        return PacketPlayOutVehicleMove::class.java.unsafeInstance().also {
            it.setProperty("x", location.x)
            it.setProperty("y", location.y)
            it.setProperty("z", location.z)
            it.setProperty("yRot", location.yaw)
            it.setProperty("xRot", location.pitch)
        }
    }

    override fun createViewCentre(data: PacketData): Any {
        val x = data.read<Int>("x")
        val z = data.read<Int>("z")
        return PacketPlayOutViewCentre(x, z)
    }

    override fun createViewDistance(data: PacketData): Any {
        val radius = data.read<Int>("radius");
        return PacketPlayOutViewDistance(radius)
    }

    override fun createWindowData(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val id = data.read<Int>("id")
        val value = data.read<Int>("value")
        return PacketPlayOutWindowData(containerId, id, value)
    }

    override fun createWindowItems(data: PacketData): Any {
        val containerId = data.read<Int>("containerId")
        val stateId = data.read<Int>("stateId")
        val items = NonNullList.a<ItemStack>()
        data.readOrNull<List<org.bukkit.inventory.ItemStack>>("items")
            ?.forEach { i -> items.add(toNMSItem(i) as ItemStack) }
        val carriedItem =
            toNMSItem(data.readOrElse("emptyItemStack", org.bukkit.inventory.ItemStack(Material.AIR))) as ItemStack
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
        val id = data.read<Int>("id")
        val uuid = data.read<UUID>("uuid")
        val location = data.read<Location>("location")
        val direction = data.readEnumOrElse(EnumDirection::class.java, "direction", EnumDirection.NORTH)
        val motive = data.readOrElse("motive", 0)

        return PacketPlayOutSpawnEntityPainting::class.java.unsafeInstance().also {
            it.setProperty("id", id)
            it.setProperty("uuid", uuid)
            it.setProperty("pos", location.toPosition())
            it.setProperty("direction", direction)
            it.setProperty("motive", motive)
        }
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): Any {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    fun Location.toPosition(): BlockPosition {
        return BlockPosition(blockX, blockY, blockZ)
    }

    fun getDataWatcher(value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BYTE), value)
    }

    fun getDataWatcher(value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.INT), value)
    }

    fun getDataWatcher(value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.FLOAT), value)
    }

    fun getDataWatcher(value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.STRING), value)
    }

    fun getDataWatcher(value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.COMPONENT), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(value: Optional<*>): DataWatcher.Item<out Optional<*>> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(
                DataWatcher.a(
                    Entity::class.java,
                    DataWatcherRegistry.OPTIONAL_COMPONENT
                ), value as Optional<IChatBaseComponent>
            )

            is IBlockData -> DataWatcher.Item(
                DataWatcher.a(Entity::class.java, DataWatcherRegistry.BLOCK_STATE),
                value as Optional<IBlockData>
            )

            is BlockPosition -> DataWatcher.Item(
                DataWatcher.a(
                    Entity::class.java,
                    DataWatcherRegistry.OPTIONAL_BLOCK_POS
                ), value as Optional<BlockPosition>
            )

            is UUID -> DataWatcher.Item(
                DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_UUID),
                value as Optional<UUID>
            )

            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcher(value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.ITEM_STACK), value)
    }

    fun getDataWatcher(value: Boolean): DataWatcher.Item<Boolean> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BOOLEAN), value)
    }

    fun getDataWatcher(value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.PARTICLE), value)
    }

    fun getDataWatcher(value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.ROTATIONS), value)
    }

    fun getDataWatcher(value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.BLOCK_POS), value)
    }

    fun getDataWatcher(value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.DIRECTION), value)
    }

    fun getDataWatcher(value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.COMPOUND_TAG), value)
    }

    fun getDataWatcher(value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.VILLAGER_DATA), value)
    }

    fun getDataWatcher(value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.OPTIONAL_UNSIGNED_INT), value)
    }

    fun getDataWatcher(value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcher.a(Entity::class.java, DataWatcherRegistry.POSE), value)
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