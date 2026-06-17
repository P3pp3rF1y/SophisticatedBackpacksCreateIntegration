package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.IBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.network.BackpackCreatePacketHandler;
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
		if (isTextBoxFocused()) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		boolean backpackKeyPressed = KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
		if (keyCode == 256 || backpackKeyPressed) {
			if (keyCode != 256 && backpackKeyPressed && getFocused() != null && !clearFocusedWidget()) {
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
			if (!getMenu().isFirstLevelStorage()) {
				BackpackCreatePacketHandler.INSTANCE.sendToServer(OpenMountedBackpackInventoryMessage.INSTANCE);
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
