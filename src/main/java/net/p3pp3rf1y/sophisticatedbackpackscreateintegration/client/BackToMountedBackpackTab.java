package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.client;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.*;

public class BackToMountedBackpackTab extends Tab {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 80), Dimension.SQUARE_16);

	protected BackToMountedBackpackTab(Position position) {
		super(position, Component.translatable(SBPTranslationHelper.INSTANCE.translGui("back_to_backpack.tooltip")),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
	}

	@Override
	protected void onTabIconClicked(int button) {
		SBPPacketHandler.INSTANCE.sendToServer(OpenMountedBackpackInventoryMessage.INSTANCE);
	}
}
