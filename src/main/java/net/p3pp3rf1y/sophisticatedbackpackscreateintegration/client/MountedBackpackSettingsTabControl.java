package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import net.minecraft.core.BlockPos;
import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackSettingsTabControl;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

public class MountedBackpackSettingsTabControl extends BackpackSettingsTabControl {
	protected MountedBackpackSettingsTabControl(MountedBackpackSettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		int contraptionEntityId = -1;
		BlockPos localPos = BlockPos.ZERO;
		if (screen.getMenu() instanceof MountedBackpackSettingsContainerMenu menu) {
			contraptionEntityId = menu.getContraptionEntityId();
			localPos = menu.getLocalPos();
		}
		return new BackToMountedBackpackTab(new Position(x, getTopY()));
	}
}
