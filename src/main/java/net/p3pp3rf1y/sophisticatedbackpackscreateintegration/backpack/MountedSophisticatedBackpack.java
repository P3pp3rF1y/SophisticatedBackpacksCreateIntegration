package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeRenderer;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeRenderRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageUpdatePayload;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeRenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.renderdata.TankPosition;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeRenderDataType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.OptionalInt;

import static net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock.*;

public class MountedSophisticatedBackpack extends MountedStorageBase {
	public static final MapCodec<MountedSophisticatedBackpack> CODEC = ItemStack.OPTIONAL_CODEC.xmap(
			MountedSophisticatedBackpack::new, MountedSophisticatedBackpack::getStorageStack
	).fieldOf("value");

	private IBackpackWrapper backpackWrapper = IBackpackWrapper.Noop.INSTANCE;;

	@Nullable
	private WeakReference<Entity> contraptionEntity = null;
	private BlockPos localPos = BlockPos.ZERO;
	private Vec3 position = Vec3.ZERO;
	@Nullable
	private WeakReference<Level> level = null;
	protected boolean updateRenderAttributes = false;

	private boolean stackDirty = false;
	private boolean clearedNbt = false;

	public MountedSophisticatedBackpack(ItemStack storageStack) {
		super(ModContent.SOPHISTICATED_MOUNTED_BACKPACK_TYPE.get(), storageStack);
	}

	private void onStackChanged() {
		setStackDirty();
	}

	@Override
	public void setStorageStack(ItemStack stack) {
		super.setStorageStack(stack);
		backpackWrapper = IBackpackWrapper.Noop.INSTANCE;
		updateRenderAttributes = true;
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

	@Nullable
	private Entity getEntity() {
		return contraptionEntity == null ? null : contraptionEntity.get();
	}

	public void setContraptionEntity(Entity entity) {
		contraptionEntity = new WeakReference<>(entity);
	}

	@Override
	protected void afterInitialSync() {
		updateRenderAttributes = true;
	}

	private void refreshRenderBlockEntity() {
		Entity e = getEntity();
		if (e instanceof AbstractContraptionEntity abstractContraptionEntity
				&& abstractContraptionEntity.getContraption().presentBlockEntities.get(localPos) instanceof BackpackBlockEntity backpackBe) {
			backpackBe.setBackpack(getStorageStack());
			StructureTemplate.StructureBlockInfo blockInfo = abstractContraptionEntity.getContraption().getBlocks().get(localPos);
			if (blockInfo != null && blockInfo.state().getBlock() instanceof BackpackBlock) {
				backpackBe.setBlockState(blockInfo.state());
			}
		}
	}

	@Override
	public void updateWithSyncedStorageStack(ItemStack storageStack, boolean refreshBlockRender) {
		setStorageStack(storageStack);
		updateRenderAttributes = true;
	}

	@Override
	public IStorageWrapper getStorageWrapper() {
		if (backpackWrapper == IBackpackWrapper.Noop.INSTANCE) {
			backpackWrapper = BackpackWrapper.fromStack(getStorageStack());
			backpackWrapper.setContentsChangeHandler(this::onStackChanged);
		}

		return backpackWrapper;
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (getStorageStack().has(ModCoreDataComponents.STORAGE_UUID) && be instanceof BackpackBlockEntity backpackBe) {
			backpackBe.setBackpack(getStorageStack());
		}
	}

	private static MountedStorageContainerMenuBase createMenu(int id, Player pl, MountedBackpackContext context) {
		return new MountedBackpackContainerMenu(id, pl, context);
	}

	public static OptionalInt openMenu(ServerPlayer player, MountedBackpackContext context) {
		return player.openMenu(new SophisticatedMenuProvider((w, p, pl) -> createMenu(w, pl, context), context.getDisplayName(player), false), context::toBuffer);
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
		ServerLevel level = player.serverLevel();
		int contraptionEntityId = contraption.entity.getId();
		BlockPos localPos = info.pos();

		Vec3 localPosVec = Vec3.atCenterOf(localPos);

		OptionalInt id = openMenu(player, new MountedBackpackContext(contraptionEntityId, localPos));
		if (id.isPresent()) {
			Vec3 globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
			onOpen(level, globalPos);
			return true;
		} else {
			return false;
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
		if (level instanceof ServerLevel) {
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
	}

	private void runTickableUpgrades(Level level) {
		getStorageWrapper().getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class).forEach(upgrade -> upgrade.tick(getEntity(), level, new BlockPos((int) getPosition().x(), (int) getPosition().y(), (int) getPosition().z())));
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
		remainingStack = InventoryHelper.runPickupOnPickupResponseUpgrades(level, getStorageWrapper().getUpgradeHandler(), remainingStack, false);
		if (remainingStack.getCount() < itemEntity.getItem().getCount()) {
			itemEntity.setItem(remainingStack);
		}
	}

	private AABB getPickupBoundingBox() {
		return new AABB(getPosition(), getPosition().add(1, 1, 1)).inflate(0.2);
	}

	private void clientTick(Level level) {
		if (updateRenderAttributes) {
			refreshRenderBlockEntity();
			updateRenderAttributes = false;
		}
		if (level.random.nextInt(10) == 0) {
			RenderInfo renderInfo = getStorageWrapper().getRenderInfo();
			renderUpgrades(level, level.random, renderInfo);
		}
	}

	private void renderUpgrades(Level level, RandomSource rand, RenderInfo renderInfo) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		renderInfo.getUpgradeRenderData().forEach((type, data) -> UpgradeRenderRegistry.getUpgradeRenderer(type)
				.ifPresent(renderer -> renderUpgrade(renderer, level, rand, type, data)));
	}

	private <T extends IUpgradeRenderData> void renderUpgrade(IUpgradeRenderer<T> renderer, Level level, RandomSource rand, UpgradeRenderDataType<?> type, IUpgradeRenderData data) {
		//noinspection unchecked
		type.cast(data).ifPresent(renderData -> renderer.render(level, rand, vector -> vector.add((float) getPosition().x(), (float) getPosition().y() + 0.8f, (float) getPosition().z()), (T) renderData));
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
		PacketDistributor.sendToPlayersTrackingEntity(entity, new MountedStorageUpdatePayload(entity.getId(), localPos, getStorageStack(), false));
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
				cEntity.setBlock(localPos, new StructureTemplate.StructureBlockInfo(blockInfo.pos(), state, blockInfo.nbt()));
			}
		}
	}

	public void clearNbt() {
		if (!clearedNbt && getEntity() instanceof AbstractContraptionEntity abstractContraptionEntity) {
			abstractContraptionEntity.getContraption().getBlocks()
					.computeIfPresent(localPos, (p, blockInfo) -> new StructureTemplate.StructureBlockInfo(blockInfo.pos(), blockInfo.state(), null));
			clearedNbt = true;
		}
	}

	@Override
	protected IItemHandlerModifiable getExternalItemHandler() {
		return getStorageWrapper().getInventoryForInputOutput();
	}
}

