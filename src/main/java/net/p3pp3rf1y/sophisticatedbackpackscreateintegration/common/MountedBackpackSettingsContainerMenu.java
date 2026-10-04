package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackSettingsHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackSettingsPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageSettingsPayload;

import java.util.Optional;
import java.util.UUID;

public class MountedBackpackSettingsContainerMenu extends MountedStorageSettingsContainerMenuBase {
	private final MountedBackpackContext context;
	private ContainerContents.SettingsData lastLinkedSettingsData = null;

	protected MountedBackpackSettingsContainerMenu(int windowId, Player player, MountedBackpackContext context) {
		this(ModContent.MOUNTED_BACKPACK_SETTINGS_CONTAINER_TYPE.get(), windowId, player, context);
	}

	protected MountedBackpackSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, MountedBackpackContext context) {
		super(menuType, windowId, player, context.getBackpackWrapper(player), context.getContraptionEntityId(), context.getLocalPos());
		this.context = context;
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompoundOrEmpty(BackpackSettingsHandler.SETTINGS_TAG);
	}

	public static MountedBackpackSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedBackpackSettingsContainerMenu(windowId, playerInventory.player, MountedBackpackContext.fromBuffer(buffer, playerInventory.player));
	}

	public MountedBackpackContext getContext() {
		return context;
	}

	@Override
	public void detectSettingsChangeAndReload() {
		Optional<UUID> groupId = getLinkedStorageGroupId();
		if (groupId.isPresent()) {
			if (player.level().isClientSide()) {
				UUID linkedGroupId = groupId.get();
				boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(linkedGroupId);
				boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(linkedGroupId);
				if (snapshotChanged || settingsChanged) {
					ILinkedStorageContents contents = ClientLinkedStorageContents.getContents(linkedGroupId)
							.orElseThrow(() -> new IllegalStateException("Updated linked backpack group has no snapshot: " + linkedGroupId));
					storageWrapper.getSettingsHandler().reloadFrom(contents.contents().settings());
				}
			}
			return;
		}
		super.detectSettingsChangeAndReload();
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		context.close();
	}

	@Override
	protected CustomPacketPayload instantiateSettingsPayload(UUID uuid, ContainerContents.SettingsData settingsContents) {
		return new BackpackSettingsPayload(uuid, settingsContents);
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (player.level().isClientSide()) {
			return;
		}
		Optional<UUID> groupId = getLinkedStorageGroupId();
		if (player instanceof ServerPlayer serverPlayer && groupId.isPresent()) {
			ContainerContents.SettingsData settingsData = storageWrapper.getSettingsHandler().getSettingsData();
			if (lastLinkedSettingsData == null || !lastLinkedSettingsData.equals(settingsData)) {
				lastLinkedSettingsData = settingsData.copy();
				PacketDistributor.sendToPlayer(serverPlayer, new LinkedStorageSettingsPayload(groupId.get(), lastLinkedSettingsData));
			}
			return;
		}
		super.sendStorageSettingsToClient();
	}

	@Override
	protected void updateFromContents(UUID uuid) {
		BackpackStorage storage = BackpackStorage.get();
		if (storage.removeUpdatedBackpackSettingsFlag(uuid)) {
			storageWrapper.getSettingsHandler().reloadFrom(storage.getOrCreateBackpackContents(uuid).settings());
		}
	}

	private Optional<UUID> getLinkedStorageGroupId() {
		if (!(storageWrapper instanceof IBackpackWrapper backpackWrapper)) {
			return Optional.empty();
		}
		return Optional.ofNullable(backpackWrapper.getBackpack().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)).map(LinkedStorageEndpointData::groupId);
	}
}
