package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackSettingsHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackContentsMessage;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackMainSettingsCategory;
import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackMainSettingsContainer;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageSettingsContainerMenuBase;

import java.util.UUID;

public class MountedBackpackSettingsContainerMenu extends MountedStorageSettingsContainerMenuBase {
	static {
		SettingsContainerMenu.addFactory(BackpackMainSettingsCategory.NAME, BackpackMainSettingsContainer::new);
	}

	private final MountedBackpackContext context;

	protected MountedBackpackSettingsContainerMenu(int windowId, Player player, MountedBackpackContext context) {
		this(ModContent.MOUNTED_BACKPACK_SETTINGS_CONTAINER_TYPE.get(), windowId, player, context);
	}

	protected MountedBackpackSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, MountedBackpackContext context) {
		super(menuType, windowId, player, context.getBackpackWrapper(player), context.getContraptionEntityId(), context.getLocalPos());
		this.context = context;
	}

	@Override
	protected CompoundTag getSettingsTag(CompoundTag contents) {
		return contents.getCompound(BackpackSettingsHandler.SETTINGS_TAG);
	}

	public static MountedBackpackSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MountedBackpackSettingsContainerMenu(windowId, playerInventory.player, MountedBackpackContext.fromBuffer(buffer));
	}

	public MountedBackpackContext getContext() {
		return context;
	}

	@Override
	protected void updateFromContents(UUID uuid) {
		BackpackStorage storage = BackpackStorage.get();
		if (storage.removeUpdatedBackpackSettingsFlag(uuid)) {
			storageWrapper.getSettingsHandler().reloadFrom(storage.getOrCreateBackpackContents(uuid));
		}
	}

	@Override
	protected void sendSettingsToClient(UUID uuid, ServerPlayer serverPlayer, CompoundTag settingsContents) {
		SBPPacketHandler.INSTANCE.sendToClient(serverPlayer, new BackpackContentsMessage(uuid, settingsContents));
	}
}
