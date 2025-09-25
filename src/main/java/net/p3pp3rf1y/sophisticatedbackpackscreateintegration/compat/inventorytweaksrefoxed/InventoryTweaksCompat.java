package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.inventorytweaksrefoxed;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;

public class InventoryTweaksCompat implements ICompat {
	@Override
	public void init() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::sendImc);
	}

	private void sendImc(InterModEnqueueEvent evt) {
		evt.enqueueWork(() -> {
			InterModComms.sendTo("invtweaks", "blacklist-screen", () -> "net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu");
		});
	}

	@Override
	public void setup() {
		//noop
	}
}
