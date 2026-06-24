package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.network;

import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSubBackpackOpenMessage;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.OpenMountedBackpackInventoryMessage;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;

public class BackpackCreatePacketHandler extends PacketHandler {
	public static final BackpackCreatePacketHandler INSTANCE = new BackpackCreatePacketHandler(SophisticatedBackpacksCreateIntegration.MOD_ID,
			SophisticatedBackpacksCreateIntegration.getNetworkProtocolVersion());

	private BackpackCreatePacketHandler(String modId, String protocol) {
		super(modId, protocol);
	}

	@Override
	public void registerMessages() {
		registerMessage(OpenMountedBackpackInventoryMessage.class, OpenMountedBackpackInventoryMessage::encode, OpenMountedBackpackInventoryMessage::decode,
				OpenMountedBackpackInventoryMessage::onMessage);
		registerMessage(MountedSubBackpackOpenMessage.class, MountedSubBackpackOpenMessage::encode, MountedSubBackpackOpenMessage::decode,
				MountedSubBackpackOpenMessage::onMessage);
	}
}
