package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import net.p3pp3rf1y.sophisticatedbackpacks.settings.BackpackSettingsTabControl;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

public class MountedBackpackSettingsTabControl extends BackpackSettingsTabControl {
	protected MountedBackpackSettingsTabControl(MountedBackpackSettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		return new BackToMountedBackpackTab(new Position(x, getTopY()));
	}
}
