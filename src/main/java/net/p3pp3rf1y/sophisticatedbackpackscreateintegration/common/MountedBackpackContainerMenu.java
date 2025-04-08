package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackSettingsHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackContentsPayload;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

import java.util.UUID;

public class MountedBackpackContainerMenu extends MountedStorageContainerMenuBase implements ISyncedContainer {
	private final MountedBackpackContext context;
	public MountedBackpackContainerMenu(int containerId, Player player, MountedBackpackContext context) {
		this(ModContent.MOUNTED_BACKPACK_CONTAINER_TYPE.get(), containerId, player, context);
	}

	public MountedBackpackContainerMenu(MenuType<?> menuType, int containerId, Player player, MountedBackpackContext context) {
		super(menuType, containerId, player, context.getBackpackWrapper(player), context.getParentBackpackWrapper(player).orElse(NoopStorageWrapper.INSTANCE), -1, false, context.getContraptionEntityId(), context.getLocalPos());
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
	protected MountedStorageSettingsContainerMenuBase instantiateSettingsContainerMenu(int windowId, Player player, int contraptionEntityId, BlockPos localPos) {
		return new MountedBackpackSettingsContainerMenu(windowId, player, context);
	}

	@Override
	protected void writeSettingsContainerMenuExtraData(FriendlyByteBuf buffer) {
		context.toBuffer(buffer);
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
	protected String getSettingsTitleKey() {
		return SBPTranslationHelper.INSTANCE.translGui("settings.title");
	}

	public MountedBackpackContext getContext() {
		return context;
	}
}
