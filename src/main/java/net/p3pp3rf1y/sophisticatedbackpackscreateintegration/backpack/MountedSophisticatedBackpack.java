package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackLinkedStorageResolver;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.LinkedStorageBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.LinkedStorageBackpackContentsMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeRenderer;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeRenderRegistry;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageUpdateMessage;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.EnderLinkerItem;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageItemInteractionTarget;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeRenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.renderdata.TankPosition;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeRenderDataType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

import javax.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import static net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock.BATTERY;
import static net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock.LEFT_TANK;
import static net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock.RIGHT_TANK;

public class MountedSophisticatedBackpack extends MountedStorageBase implements ILinkedStorageItemInteractionTarget {
	public static final Codec<MountedSophisticatedBackpack> CODEC = ItemStack.CODEC.xmap(MountedSophisticatedBackpack::new,
			MountedSophisticatedBackpack::getStorageStack);

	private IBackpackWrapper backpackWrapper = IBackpackWrapper.Noop.INSTANCE;

	@Nullable
	private WeakReference<Entity> contraptionEntity = null;
	private BlockPos localPos = BlockPos.ZERO;
	private Vec3 position = Vec3.ZERO;
	@Nullable
	private WeakReference<Level> level = null;
	protected boolean updateRenderAttributes = false;

	private boolean stackDirty = false;
	private boolean blockRenderDirty = false;
	private boolean clearedNbt = false;

	public MountedSophisticatedBackpack(ItemStack storageStack) {
		super(ModContent.SOPHISTICATED_MOUNTED_BACKPACK_TYPE.get(), storageStack);
	}

	private void onStackChanged() {
		setStackDirty();
	}

	@Override
	public void setStorageStack(ItemStack stack) {
		if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper
				&& linkedStorageBackpackWrapper.hasEndpoint(LinkedStorageStackData.getEndpoint(stack))
				&& linkedStorageBackpackWrapper.getBackpack().is(stack.getItem())) {
			linkedStorageBackpackWrapper.replacePhysicalBackpackStack(stack);
		} else if (backpackWrapper instanceof BackpackWrapper regularBackpackWrapper && hasSameStorage(stack, regularBackpackWrapper)) {
			regularBackpackWrapper.replaceBackpackStack(stack);
		} else {
			closeBackpackWrapper();
		}
		super.setStorageStack(stack);
		updateRenderAttributes = true;
	}

	private static boolean hasSameStorage(ItemStack stack, BackpackWrapper backpackWrapper) {
		ItemStack currentStack = backpackWrapper.getBackpack();
		return currentStack.is(stack.getItem()) && currentStack.hasTag() && stack.hasTag() && Objects.equals(
				NBTHelper.getUniqueId(currentStack, BackpackWrapper.CONTENTS_UUID_TAG), NBTHelper.getUniqueId(stack, BackpackWrapper.CONTENTS_UUID_TAG));
	}

	private void setStackDirty() {
		stackDirty = true;
	}

	private void setStackClean() {
		stackDirty = false;
	}

	public static MountedSophisticatedBackpack from(BackpackBlockEntity backpackBe) {
		return new MountedSophisticatedBackpack(backpackBe.getBackpackWrapper().getBackpack());
	}

	public void initializeStorage(Level level) {
		setLevel(level);
		getStorageWrapper();
	}

	@Nullable
	private Entity getEntity() {
		return contraptionEntity == null ? null : contraptionEntity.get();
	}

	public void setContraptionEntity(Entity entity) {
		contraptionEntity = new WeakReference<>(entity);
	}

	private boolean refreshRenderBlockEntity() {
		Entity e = getEntity();
		if (e instanceof AbstractContraptionEntity abstractContraptionEntity
				&& abstractContraptionEntity.getContraption().getBlockEntityClientSide(localPos) instanceof BackpackBlockEntity backpackBe) {
			backpackBe.setBackpack(getStorageStack());
			StructureTemplate.StructureBlockInfo blockInfo = abstractContraptionEntity.getContraption().getBlocks().get(localPos);
			if (blockInfo != null && blockInfo.state().getBlock() instanceof BackpackBlock) {
				backpackBe.setBlockState(blockInfo.state());
			}
			abstractContraptionEntity.getContraption().invalidateClientContraptionStructure();
			return true;
		}
		return false;
	}

	@Override
	public void updateWithSyncedStorageStack(ItemStack storageStack, boolean refreshBlockRender) {
		int previousPhysicalColumnsTaken = getStorageStack().getOrCreateTag().getInt("columnsTaken");
		setStorageStack(storageStack);
		refreshOpenMenuClientInfo(previousPhysicalColumnsTaken);
		updateRenderAttributes = true;
	}

	@Override
	public IStorageWrapper getStorageWrapper() {
		if (backpackWrapper == IBackpackWrapper.Noop.INSTANCE) {
			backpackWrapper = createBackpackWrapper();
		}

		return backpackWrapper;
	}

	public IStorageWrapper getStorageWrapperForMenu() {
		getStorageWrapper();
		Level level = getLevel();
		if (level != null && LinkedStorageStackData.getEndpoint(getStorageStack()) != null
				&& (level.isClientSide || !(backpackWrapper instanceof LinkedStorageBackpackWrapper))) {
			BackpackLinkedStorageResolver.resolve(level, getStorageStack()).ifPresent(this::replaceBackpackWrapper);
		}
		return backpackWrapper;
	}

	private IBackpackWrapper createBackpackWrapper() {
		Level level = getLevel();
		IBackpackWrapper wrapper = level == null
				? new BackpackWrapper(getStorageStack())
				: BackpackLinkedStorageResolver.resolveOrCreate(level, getStorageStack());
		configureBackpackWrapper(wrapper);
		return wrapper;
	}

	private void replaceBackpackWrapper(IBackpackWrapper wrapper) {
		closeBackpackWrapper();
		backpackWrapper = wrapper;
		configureBackpackWrapper(wrapper);
	}

	private void configureBackpackWrapper(IBackpackWrapper wrapper) {
		wrapper.setContentsChangeHandler(this::onStackChanged);
		if (!(wrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper)) {
			return;
		}

		linkedStorageBackpackWrapper.setCanonicalContentsChangedHandler(this::onLinkedStorageContentsChanged);
		Level level = getLevel();
		if (level instanceof ServerLevel serverLevel) {
			linkedStorageBackpackWrapper.onInit(level);
			setStackDirty();
			if (linkedStorageBackpackWrapper.synchronizePhysicalProjection(serverLevel)) {
				onLinkedStorageContentsChanged();
			}
		}
	}

	private void onLinkedStorageContentsChanged() {
		setStackDirty();
		blockRenderDirty = true;
		refreshBlockRenderState();
		syncLinkedContentsToOpenMenus();
		sendStorageUpdatePayload();
	}

	private void syncLinkedContentsToOpenMenus() {
		if (!(getLevel() instanceof ServerLevel serverLevel) || !(backpackWrapper instanceof LinkedStorageBackpackWrapper)) {
			return;
		}

		LinkedStorageEndpointData endpoint = LinkedStorageStackData.getEndpoint(getStorageStack());
		Entity entity = getEntity();
		if (endpoint == null || entity == null) {
			return;
		}

		for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
			if (player.serverLevel() == serverLevel && isOpenMountedBackpackMenu(player, entity.getId())) {
				SBPPacketHandler.INSTANCE.sendToClient(player, LinkedStorageBackpackContentsMessage.createSnapshot(serverLevel, endpoint.groupId()));
			}
		}
	}

	private boolean isOpenMountedBackpackMenu(ServerPlayer player, int contraptionEntityId) {
		if (player.containerMenu instanceof MountedBackpackContainerMenu menu) {
			return menu.getContext().getContraptionEntityId() == contraptionEntityId && menu.getContext().getLocalPos().equals(localPos);
		}
		if (player.containerMenu instanceof MountedBackpackSettingsContainerMenu menu) {
			return menu.getContext().getContraptionEntityId() == contraptionEntityId && menu.getContext().getLocalPos().equals(localPos);
		}
		return false;
	}

	private void refreshBlockRenderState() {
		if (!blockRenderDirty || getEntity() == null) {
			return;
		}

		setBlockRenderDirty();
		blockRenderDirty = false;
	}

	private void closeBackpackWrapper() {
		if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper) {
			linkedStorageBackpackWrapper.close();
		}
		backpackWrapper = IBackpackWrapper.Noop.INSTANCE;
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (be instanceof BackpackBlockEntity backpackBe) {
			backpackBe.setBackpack(getStorageStack());
		}
		closeBackpackWrapper();
	}

	@Override
	public void onContraptionDestroyed() {
		closeBackpackWrapper();
	}

	@Override
	public void onContraptionRemoved() {
		closeBackpackWrapper();
	}

	private static MountedStorageContainerMenuBase createMenu(int id, Player pl, MountedBackpackContext context) {
		return new MountedBackpackContainerMenu(id, pl, context);
	}

	public static void openMenu(ServerPlayer player, MountedBackpackContext context) {
		NetworkHooks.openScreen(player, new SimpleMenuProvider((w, p, pl) -> createMenu(w, pl, context), context.getDisplayName(player)),
				buffer -> context.toBuffer(buffer, player));
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
		if (tryLinkBackpack(player, contraption, info)) {
			return true;
		}

		ServerLevel level = player.serverLevel();
		int contraptionEntityId = contraption.entity.getId();
		BlockPos localPos = info.pos();

		Vec3 localPosVec = Vec3.atCenterOf(localPos);

		openMenu(player, new MountedBackpackContext(contraptionEntityId, localPos));
		Vec3 globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
		onOpen(level, globalPos);
		return true;
	}

	private boolean tryLinkBackpack(ServerPlayer player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
		ItemStack linker = player.getMainHandItem();
		if (!(linker.getItem() instanceof EnderLinkerItem)) {
			return false;
		}

		setContraptionEntity(contraption.entity);
		setLocalPos(info.pos());
		setLevel(player.serverLevel());
		BlockPos feedbackPos = BlockPos.containing(contraption.entity.toGlobalVector(Vec3.atCenterOf(info.pos()), 0));
		return EnderLinkerItem.tryLinkItemInteractionTarget(player, linker, this, feedbackPos).isPresent();
	}

	@Override
	public ItemStack getLinkedStorageItem() {
		return getStorageStack();
	}

	@Override
	public void onLinkedStorageEndpointChanged(ServerPlayer player, @Nullable LinkedStorageEndpointData previousEndpoint,
			@Nullable LinkedStorageEndpointData currentEndpoint) {
		Entity entity = getEntity();
		if (entity != null) {
			closeOpenMenus(player.serverLevel(), entity.getId(), localPos);
		}
		closeBackpackWrapper();
		getStorageWrapper();
	}

	private static void closeOpenMenus(ServerLevel level, int contraptionEntityId, BlockPos localPos) {
		for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
			if (player.serverLevel() != level) {
				continue;
			}
			MountedBackpackContext context = null;
			if (player.containerMenu instanceof MountedBackpackContainerMenu menu) {
				context = menu.getContext();
			} else if (player.containerMenu instanceof MountedBackpackSettingsContainerMenu menu) {
				context = menu.getContext();
			}
			if (context != null && context.getContraptionEntityId() == contraptionEntityId && context.getLocalPos().equals(localPos)) {
				player.closeContainer();
			}
		}
	}

	protected Vec3 getPosition() {
		return position;
	}

	public void setPosition(Vec3 position) {
		this.position = position;
	}

	void initEntityLevelAndPositions(MovementContext context) {
		if (getEntity() == null) {
			AbstractContraptionEntity entity = context.contraption.entity;
			BlockPos localPos = context.localPos;
			Vec3 position = context.position;
			Level level = context.world;
			initEntityLevelAndPositions(entity, localPos, level, position);
		}
	}

	public void initEntityLevelAndPositions(AbstractContraptionEntity abstractContraptionEntity, BlockPos localPos, Level level, Vec3 position) {
		setContraptionEntity(abstractContraptionEntity);
		setLocalPos(localPos);
		setLevel(level);
		setPosition(position);
		if (level instanceof ServerLevel serverLevel) {
			getStorageWrapper();
			synchronizeLinkedStorageProjection(serverLevel);
			blockRenderDirty = true;
		}
		refreshBlockRenderState();
		refreshOpenMenuClientInfo(getStorageStack().getOrCreateTag().getInt("columnsTaken"));
		if (level.isClientSide && updateRenderAttributes && refreshRenderBlockEntity()) {
			updateRenderAttributes = false;
		}
		if (level instanceof ServerLevel) {
			sendStorageUpdatePayload();
		}
	}

	public void setLocalPos(BlockPos localPos) {
		this.localPos = localPos;
	}

	public void setLevel(Level level) {
		this.level = new WeakReference<>(level);
	}

	@Nullable
	protected Level getLevel() {
		return level == null ? null : level.get();
	}

	public void tick() {
		Level level = getLevel();
		if (level instanceof ServerLevel serverLevel) {
			synchronizeLinkedStorageProjection(serverLevel);
			sendStorageUpdatePayload();
		}

		if (level == null) {
			return;
		}
		if (level.isClientSide()) {
			clientTick(level);
			return;
		}
		runTickableUpgrades(level);
		runPickupOnItemEntities(level);
		refreshBlockRenderState();
	}

	private void synchronizeLinkedStorageProjection(ServerLevel level) {
		if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper
				&& linkedStorageBackpackWrapper.synchronizePhysicalProjection(level)) {
			onLinkedStorageContentsChanged();
		}
	}

	private void runTickableUpgrades(Level level) {
		getWrapperForGlobalUpgradeProcessing(level).getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)
				.forEach(upgrade -> upgrade.tick(getEntity(), level, new BlockPos((int) getPosition().x(), (int) getPosition().y(), (int) getPosition().z())));
	}

	private IBackpackWrapper getWrapperForGlobalUpgradeProcessing(Level level) {
		if (LinkedStorageStackData.getEndpoint(getStorageStack()) != null) {
			return BackpackLinkedStorageResolver.resolveForGlobalUpgradeProcessing(level, getStorageStack());
		}
		return (IBackpackWrapper) getStorageWrapper();
	}

	private void runPickupOnItemEntities(Level level) {
		AABB aabb = getPickupBoundingBox();
		List<ItemEntity> collidedWithItemEntities = level.getEntitiesOfClass(ItemEntity.class, aabb);
		collidedWithItemEntities.forEach(itemEntity -> {
			if (itemEntity.isAlive()) {
				tryToPickup(level, itemEntity);
			}
		});
	}

	private void tryToPickup(Level level, ItemEntity itemEntity) {
		ItemStack remainingStack = itemEntity.getItem().copy();
		remainingStack = InventoryHelper.runPickupOnPickupResponseUpgrades(level, getWrapperForGlobalUpgradeProcessing(level).getUpgradeHandler(),
				remainingStack, false);
		if (remainingStack.getCount() < itemEntity.getItem().getCount()) {
			itemEntity.setItem(remainingStack);
		}
	}

	private AABB getPickupBoundingBox() {
		return new AABB(getPosition(), getPosition().add(1, 1, 1)).inflate(0.2);
	}

	private void clientTick(Level level) {
		if (updateRenderAttributes && refreshRenderBlockEntity()) {
			updateRenderAttributes = false;
		}
		if (level.random.nextInt(10) == 0) {
			RenderInfo renderInfo = getStorageWrapper().getRenderInfo();
			renderUpgrades(level, level.random, renderInfo);
		}
	}

	private void refreshOpenMenuClientInfo(int previousPhysicalColumnsTaken) {
		Level level = getLevel();
		if (level == null || !level.isClientSide
				|| !(Minecraft.getInstance().player != null && Minecraft.getInstance().player.containerMenu instanceof MountedBackpackContainerMenu menu)) {
			return;
		}

		getStorageWrapper();
		MountedBackpackContext context = menu.getContext();
		Entity entity = getEntity();
		if (entity != null && context.getContraptionEntityId() == entity.getId() && context.getLocalPos().equals(localPos)) {
			int columnsTaken = getStorageStack().getOrCreateTag().getInt("columnsTaken");
			menu.syncClientInfo(backpackWrapper.getRenderInfo().getNbt(), previousPhysicalColumnsTaken, columnsTaken);
		}
	}

	private void renderUpgrades(Level level, RandomSource rand, RenderInfo renderInfo) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		renderInfo.getUpgradeRenderData().forEach(
				(type, data) -> UpgradeRenderRegistry.getUpgradeRenderer(type).ifPresent(renderer -> renderUpgrade(renderer, level, rand, type, data)));
	}

	private <T extends IUpgradeRenderData> void renderUpgrade(IUpgradeRenderer<T> renderer, Level level, RandomSource rand, UpgradeRenderDataType<?> type,
			IUpgradeRenderData data) {
		// noinspection unchecked
		type.cast(data).ifPresent(renderData -> renderer.render(level, rand,
				vector -> vector.add((float) getPosition().x(), (float) getPosition().y() + 0.8f, (float) getPosition().z()), (T) renderData));
	}

	private boolean isStackDirty() {
		return stackDirty;
	}

	public void sendStorageUpdatePayload() {
		if (!isStackDirty()) {
			return;
		}
		Entity entity = getEntity();
		if (entity == null || entity.level().isClientSide()) {
			return;
		}

		setStackClean();
		PacketHandler.INSTANCE.sendToAllTracking(new MountedStorageUpdateMessage(entity.getId(), localPos, getStorageStack(), false), entity);
	}

	public void setBlockRenderDirty() {
		if (getEntity() instanceof AbstractContraptionEntity cEntity) {
			StructureTemplate.StructureBlockInfo blockInfo = cEntity.getContraption().getBlocks().get(localPos);
			if (blockInfo != null && blockInfo.state().getBlock() instanceof BackpackBlock) {
				BlockState state = blockInfo.state();
				state = state.setValue(LEFT_TANK, false);
				state = state.setValue(RIGHT_TANK, false);
				RenderInfo renderInfo = backpackWrapper.getRenderInfo();
				for (TankPosition pos : renderInfo.getTankRenderInfos().keySet()) {
					if (pos == TankPosition.LEFT) {
						state = state.setValue(LEFT_TANK, true);
					} else if (pos == TankPosition.RIGHT) {
						state = state.setValue(RIGHT_TANK, true);
					}
				}
				state = state.setValue(BATTERY, renderInfo.getBatteryRenderInfo().isPresent());
				if (!state.equals(blockInfo.state())) {
					cEntity.setBlock(localPos, new StructureTemplate.StructureBlockInfo(blockInfo.pos(), state, blockInfo.nbt()));
				}
			}
		}
	}

	public void clearNbt() {
		if (!clearedNbt && getEntity() instanceof AbstractContraptionEntity abstractContraptionEntity) {
			abstractContraptionEntity.getContraption().getBlocks().computeIfPresent(localPos,
					(p, blockInfo) -> new StructureTemplate.StructureBlockInfo(blockInfo.pos(), blockInfo.state(), null));
			clearedNbt = true;
		}
	}

	@Override
	protected IItemHandlerModifiable getExternalItemHandler() {
		return getStorageWrapper().getInventoryForInputOutput();
	}
}
