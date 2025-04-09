package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;

public class MountedBackpackSettingsScreen extends BackpackSettingsScreen {
	public MountedBackpackSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component title) {
		super(screenContainer, inv, title);
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new MountedBackpackSettingsTabControl(this, new Position(leftPos + imageWidth, topPos + 4));
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		SBPPacketHandler.INSTANCE.sendToServer(OpenMountedBackpackInventoryMessage.INSTANCE);
	}

	public static MountedBackpackSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory inventory, Component title) {
		return new MountedBackpackSettingsScreen(screenContainer, inventory, title);
	}
}
