package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.inventorytweaksrefoxed;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;

public class InventoryTweaksCompat implements ICompat {
	@Override
	public void init(IEventBus modBus) {
		ICompat.super.init(modBus);
		modBus.addListener(this::sendImc);
	}

	private void sendImc(InterModEnqueueEvent evt) {
		evt.enqueueWork(() -> {
			InterModComms.sendTo("invtweaks", "blacklist-screen",
					() -> "net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu");
		});
	}

	@Override
	public void setup() {
		// noop
	}
}
