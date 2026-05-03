package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.compat.trashslot;

import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.init.ModContent;
import net.p3pp3rf1y.sophisticatedcore.compat.ICompat;

public class TrashSlotCompat implements ICompat {
	@Override
	public void setup() {
		net.p3pp3rf1y.sophisticatedcore.compat.trashslot.TrashSlotCompat.registerMenuType(ModContent.MOUNTED_BACKPACK_CONTAINER_TYPE.get());
	}
}
