package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init;

import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.inventorytweaksrefoxed.InventoryTweaksCompat;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.trashslot.TrashSlotCompat;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatInfo;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatModIds;
import net.p3pp3rf1y.sophisticatedcore.compat.CompatRegistry;

public class ModCompat {
	private ModCompat() {
	}

	public static void register() {
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.INVENTORY_TWEAKS), () -> mobBus -> new InventoryTweaksCompat());
		CompatRegistry.registerCompat(new CompatInfo(CompatModIds.TRASH_SLOT), () -> mobBus -> new TrashSlotCompat());
	}
}

