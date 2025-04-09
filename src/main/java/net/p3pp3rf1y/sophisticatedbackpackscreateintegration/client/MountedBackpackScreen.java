package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.IBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public class MountedBackpackScreen extends StorageScreenBase<MountedBackpackContainerMenu> implements IBackpackScreen {
	public static MountedBackpackScreen constructScreen(MountedBackpackContainerMenu screenContainer, Inventory inv, Component title) {
		return new MountedBackpackScreen(screenContainer, inv, title);
	}

	protected MountedBackpackScreen(MountedBackpackContainerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected String getStorageSettingsTabTooltip() {
		return SBPTranslationHelper.INSTANCE.translGui("settings.tooltip");
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (getFocused() != null) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		if (keyCode == 256 || KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
			if (!getMenu().isFirstLevelStorage()) {
				SBPPacketHandler.INSTANCE.sendToServer(OpenMountedBackpackInventoryMessage.INSTANCE);
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
