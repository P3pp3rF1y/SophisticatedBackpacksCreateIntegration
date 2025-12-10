package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.IBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryPayload;
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
		return BackpackTranslationHelper.INSTANCE.translGui("settings.tooltip");
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (getFocused() != null) {
			return super.keyPressed(event);
		}
		if (event.key() == 256 || KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(InputConstants.getKey(event))) {
			if (!getMenu().isFirstLevelStorage()) {
				ClientPacketDistributor.sendToServer(OpenMountedBackpackInventoryPayload.INSTANCE);
				return true;
			}
		}
		return super.keyPressed(event);
	}
}
