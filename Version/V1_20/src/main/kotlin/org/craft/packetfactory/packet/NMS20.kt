package org.craft.packetfactory.packet

import net.minecraft.core.BlockPosition
import net.minecraft.core.EnumDirection
import net.minecraft.core.NonNullList
import net.minecraft.core.Vector3f
import net.minecraft.core.particles.ParticleParam
import net.minecraft.core.registries.BuiltInRegistries
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
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import org.craft.packetfactory.data.PacketData
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.reflex.Reflex.Companion.unsafeInstance
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.remap.require
import java.util.*
import kotlin.jvm.optionals.getOrNull

internal class NMS20 : NMSOut {
    /**
     * 创建实体生成数据包
     *
     * @param data 包含实体生成信息的数据对象，需要包含以下字段：
     *             - entityId: Int 实体ID（必需）
     *             - uuid: UUID 实体UUID（必需）
     *             - entityType: EntityType 实体类型（必需）
     *             - location: Location 生成位置（必需）
     *             - extraData: Int 额外数据（可选，默认0）
     *             - yHeadRot: Double 头部旋转（可选，默认0.0）
     * @return PacketPlayOutSpawnEntity 实体生成数据包实例
     */
    override fun createSpawnEntity(data: PacketData): Any {
        val entityId = data.read<Int>("entityId")
        val uuid = data.read<UUID>("uuid")
        val entityType = data.read<EntityType>("entityType")
        val location = data.read<Location>("location")
        val extraData = data.readOrElse("extraData", 0)
        val yHeadRot = data.readOrElse("yHeadRot", 0.0)

        var type = BuiltInRegistries.ENTITY_TYPE[MinecraftKey(entityType.name.lowercase())]

        /** 暂时先不处理 1.20.5 版本 */
        if (MinecraftVersion.versionId >= 12005&&false) {
            type = BuiltInRegistries.ENTITY_TYPE.byId(entityType.typeId.toInt())
        }
        return PacketPlayOutSpawnEntity(
            entityId,
            uuid,
            location.x,
            location.y,
            location.z,
            fixYaw(entityType, location.yaw),
            location.pitch,
            type,
            extraData,
            Vec3D.ZERO,
            yHeadRot
        )
    }

    override fun createSpawnEntityLiving(data: PacketData): Any {
        TODO("该版本不再使用该包生成实体,使用createSpawnEntity方法生成实体")
    }

    /**
     * 创建经验球生成网络数据包
     *
     * @param data 包含经验球生成信息的数据对象，需要包含以下字段：
     *             - id: Int 经验球网络ID（必需）
     *             - location: Location 世界生成坐标（必需）
     *             - value: Int 经验点数（可选，默认0）
     * @return PacketPlayOutSpawnEntityExperienceOrb 经验球生成S2C数据包
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
        TODO("Not yet implemented")
    }

    override fun createEntityHeadRotation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityMetadata(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityDestroy(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRelEntityMove(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityLook(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createRelEntityMoveLook(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityEquipment(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAnimation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAttachEntity(data: PacketData): Any {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun createSelectKnown(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createUpdateEnabledFeatures(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createAcceptCodeOfConduct(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCookieRequest(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCookieResponse(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBlockChangedAck(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBundleDelimiter(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createChunkBatchFinished(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createChunkBatchStart(data: PacketData): Any {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun createSetBorderCenter(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetBorderLerpSize(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetBorderSize(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetBorderWarningDelay(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetBorderWarningDistance(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetCursorItem(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetPlayerInventory(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetSimulationDistance(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createSetSubtitleText(data: PacketData): Any {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun createBlockAction(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBlockBreakAnimation(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBlockChange(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createBossBar(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCamera(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCloseWindow(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCollect(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createCommands(data: PacketData): Any {
        TODO("Not yet implemented")
    }

    override fun createEntityEffect(data: PacketData): Any {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * 创建客户端视图中心网络数据包
     *
     * @param data 包含视图中心信息的数据对象，需要包含以下字段：
     *             - x: Int 视图中心区块X坐标（必需）
     *             - z: Int 视图中心区块Z坐标（必需）
     * @return PacketPlayOutViewCentre 客户端视图中心S2C数据包
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
        val items = NonNullList.create<ItemStack>()
        data.readOrNull<List<org.bukkit.inventory.ItemStack>>("items")?.forEach { i -> items.add(toNMSItem(i)) }
        val carriedItem = toNMSItem(data.readOrElse("emptyItemStack", org.bukkit.inventory.ItemStack(Material.AIR)))
        return PacketPlayOutWindowItems(containerId, stateId, items, carriedItem)
    }

    /**
     * 创建世界音效/粒子事件网络数据包
     *
     * @param data 包含世界事件信息的数据对象，需要包含以下字段：
     *             - type: Int 事件类型ID（必需）
     *             - location: Location 世界事件坐标（必需）
     *             - data: Int 事件附加数据（可选，默认0）
     *             - globalEvent: Boolean 是否广播到所有玩家（可选，默认false）
     * @return PacketPlayOutWorldEvent 世界事件S2C数据包
     */
    override fun createWorldEvent(data: PacketData): Any {
        val type = data.read<Int>("type")
        val location = data.read<Location>("location").toPosition()
        val dataValue = data.readOrElse("data", 0)
        val globalEvent = data.readOrElse("globalEvent", false)
        return PacketPlayOutWorldEvent(type, location, dataValue, globalEvent)
    }

    /**
     * 创建世界粒子效果网络数据包
     *
     * @param data 包含粒子效果信息的数据对象，需要包含以下字段：
     *             - type: Particle 粒子类型（必需）
     *             - overrideLimiter: Boolean 是否忽略客户端粒子限制（可选，默认false）
     *             - location: Location 粒子生成坐标（必需）
     *             - vector: Vector 粒子运动向量（可选，默认零向量）
     *             - maxSpeed: Float 粒子最大扩散速度（可选，默认1.0f）
     *             - count: Int 粒子生成数量（可选，默认1）
     * @return PacketPlayOutWorldParticles 世界粒子效果S2C数据包
     */
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

    /**
     * 创建命名实体生成数据包
     *
     * @param data 包含实体生成信息的数据对象，需要包含以下字段：
     *             - entityId: Int 实体ID（必需）
     *             - uuid: UUID 实体UUID（必需）
     *             - location: Location 生成位置（必需）
     * @return PacketPlayOutNamedEntitySpawn 命名实体生成数据包实例
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
        TODO("1.19+ 不再支持发Painting包")
    }

    private fun toNMSItem(itemStack: org.bukkit.inventory.ItemStack): ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    fun Location.toPosition(): BlockPosition {
        return BlockPosition(blockX, blockY, blockZ)
    }

    fun getDataWatcherItem(value: Byte): DataWatcher.Item<Byte> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.BYTE), value)
    }

    fun getDataWatcherItem(value: Int): DataWatcher.Item<Int> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.INT), value)
    }

    fun getDataWatcherItem(value: Float): DataWatcher.Item<Float> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.FLOAT), value)
    }

    fun getDataWatcherItem(value: String): DataWatcher.Item<String> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.STRING), value)
    }

    fun getDataWatcherItem(value: IChatBaseComponent): DataWatcher.Item<IChatBaseComponent> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.COMPONENT), value)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionalDataWatcher(value: Optional<*>): DataWatcher.Item<out Optional<out Any?>?> {
        return when (value.getOrNull()) {
            is IChatBaseComponent -> DataWatcher.Item(
                DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.OPTIONAL_COMPONENT),
                value as Optional<IChatBaseComponent>
            )

            is BlockPosition -> DataWatcher.Item(
                DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.OPTIONAL_BLOCK_POS),
                value as Optional<BlockPosition>
            )

            is UUID -> DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.OPTIONAL_UUID), value as Optional<UUID>)

            else -> error("不支持的类型: $value")
        }
    }

    fun getDataWatcherItem(value: ItemStack): DataWatcher.Item<ItemStack> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.ITEM_STACK), value)
    }

    fun getDataWatcherItem(value: Boolean): DataWatcher.Item<Boolean> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.BOOLEAN), value)
    }

    fun getDataWatcherItem(value: ParticleParam): DataWatcher.Item<ParticleParam> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.PARTICLE), value)
    }

    fun getDataWatcherItem(value: Vector3f): DataWatcher.Item<Vector3f> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.ROTATIONS), value)
    }

    fun getDataWatcherItem(value: BlockPosition): DataWatcher.Item<BlockPosition> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.BLOCK_POS), value)
    }

    fun getDataWatcherItem(value: EnumDirection): DataWatcher.Item<EnumDirection> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.DIRECTION), value)
    }

    fun getDataWatcherItem(value: NBTTagCompound): DataWatcher.Item<NBTTagCompound> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.COMPOUND_TAG), value)
    }

    fun getDataWatcherItem(value: VillagerData): DataWatcher.Item<VillagerData> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.VILLAGER_DATA), value)
    }

    fun getDataWatcherItem(value: OptionalInt): DataWatcher.Item<OptionalInt> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.OPTIONAL_UNSIGNED_INT), value)
    }

    fun getDataWatcherItem(value: EntityPose): DataWatcher.Item<EntityPose> {
        return DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.POSE), value)
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
            is IBlockData -> DataWatcher.Item(DataWatcher.defineId(Entity::class.java, DataWatcherRegistry.BLOCK_STATE), value)
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