package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackSettingsHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.ClientLinkedStorageBackpackContents;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.LinkedStorageBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackContentsPayload;
import net.p3pp3rf1y.sophisticatedbackpacks.network.LinkedStorageBackpackContentsPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedBackpackClientInfoPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageContentsBinding;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

import java.util.Optional;
import java.util.UUID;

public class MountedBackpackContainerMenu extends MountedStorageContainerMenuBase implements ISyncedContainer {
	private final MountedBackpackContext context;
	private CompoundTag lastLinkedSettingsNbt = null;
	public MountedBackpackContainerMenu(int containerId, Player player, MountedBackpackContext context) {
		this(ModContent.MOUNTED_BACKPACK_CONTAINER_TYPE.get(), containerId, player, context);
	}

	public MountedBackpackContainerMenu(MenuType<?> menuType, int containerId, Player player, MountedBackpackContext context) {
		super(menuType, containerId, player, context.getBackpackWrapper(player), context.getParentBackpackWrapper(player).orElse(NoopStorageWrapper.INSTANCE),
				-1, false, context.getContraptionEntityId(), context.getLocalPos());
		this.context = context;
	}

	public static MountedBackpackContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedBackpackContainerMenu(windowId, playerInventory.player, MountedBackpackContext.fromBuffer(buffer));
	}

	@Override
	protected StorageContainerMenuBase<IStorageWrapper>.StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
		return new StorageUpgradeSlot(upgradeHandler, slotIndex) {
			@Override
			protected void onUpgradeChanged() {
				if (player.level().isClientSide()) {
					return;
				}
				storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
				context.setBlockRenderDirty(player);
			}
		};
	}

	@Override
	protected MountedStorageSettingsContainerMenuBase instantiateSettingsContainerMenu(int windowId, Player player, int contraptionEntityId,
			BlockPos localPos) {
		return new MountedBackpackSettingsContainerMenu(windowId, player, context);
	}

	@Override
	protected void writeSettingsContainerMenuExtraData(FriendlyByteBuf buffer) {
		context.toBuffer(buffer, player);
	}

	@Override
	protected CustomPacketPayload instantiateSettingsPayload(UUID uuid, CompoundTag settingsContents) {
		return new BackpackContentsPayload(uuid, settingsContents);
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(BackpackSettingsHandler.SETTINGS_TAG);
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (player.level().isClientSide) {
			return;
		}

		Optional<UUID> groupId = getLinkedStorageGroupId();
		if (player instanceof ServerPlayer serverPlayer && groupId.isPresent()) {
			CompoundTag settingsNbt = storageWrapper.getSettingsHandler().getNbt();
			if (lastLinkedSettingsNbt == null || !lastLinkedSettingsNbt.equals(settingsNbt)) {
				lastLinkedSettingsNbt = settingsNbt.copy();
				PacketDistributor.sendToPlayer(serverPlayer, LinkedStorageBackpackContentsPayload.createSnapshot(serverPlayer.serverLevel(), groupId.get()));
			}
			return;
		}

		super.sendStorageSettingsToClient();
	}

	@Override
	public boolean detectSettingsChangeAndReload() {
		Optional<UUID> groupId = getLinkedStorageGroupId();
		if (groupId.isPresent()) {
			if (player.level().isClientSide && ClientLinkedStorageBackpackContents.removeUpdatedGroup(groupId.get())) {
				ILinkedStorageContentsBinding contents = ClientLinkedStorageBackpackContents.getBinding(groupId.get())
						.orElseThrow(() -> new IllegalStateException("Updated linked backpack group has no snapshot: " + groupId.get()));
				storageWrapper.getSettingsHandler().reloadFrom(contents.contents());
				return true;
			}
			return false;
		}

		return super.detectSettingsChangeAndReload();
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		context.close();
	}

	public void syncClientInfo(CompoundTag renderInfoNbt, int previousPhysicalColumnsTaken, int columnsTaken) {
		boolean columnsChanged = previousPhysicalColumnsTaken != columnsTaken;
		storageWrapper.getRenderInfo().deserializeFrom(renderInfoNbt);
		storageWrapper.setColumnsTaken(columnsTaken, false);
		if (columnsChanged) {
			storageWrapper.onContentsNbtUpdated();
			refreshAllSlots();
			onUpgradesChanged();
		}
	}

	public void syncClientProfile(CompoundTag renderInfoNbt, int columnsTaken) {
		syncClientInfo(renderInfoNbt, storageWrapper.getColumnsTaken(), columnsTaken);
	}

	@Override
	protected void onUpgradeChanged() {
		if (player instanceof ServerPlayer serverPlayer && storageWrapper instanceof IBackpackWrapper backpackWrapper
				&& !(backpackWrapper instanceof LinkedStorageBackpackWrapper)) {
			PacketDistributor.sendToPlayer(serverPlayer, new MountedBackpackClientInfoPayload(context.getContraptionEntityId(), context.getLocalPos(),
					backpackWrapper.getRenderInfo().getNbt().copy(), backpackWrapper.getColumnsTaken()));
		}
	}

	private Optional<UUID> getLinkedStorageGroupId() {
		if (!(storageWrapper instanceof IBackpackWrapper backpackWrapper)) {
			return Optional.empty();
		}
		return Optional.ofNullable(backpackWrapper.getBackpack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)).map(LinkedStorageEndpointData::groupId);
	}

	@Override
	protected String getSettingsTitleKey() {
		return BackpackTranslationHelper.INSTANCE.translGui("settings.title");
	}

	public MountedBackpackContext getContext() {
		return context;
	}
}
