package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.schematic;

import com.simibubi.create.api.schematic.nbt.SafeNbtWriterRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;

public class SophisticatedBackpackSafeNbtWriter implements SafeNbtWriterRegistry.SafeNbtWriter {
	public static final SophisticatedBackpackSafeNbtWriter INSTANCE = new SophisticatedBackpackSafeNbtWriter();

	@Override
	public void writeSafe(BlockEntity be, CompoundTag tag, HolderLookup.Provider registries) {
		if (be instanceof BackpackBlockEntity backpackBe) {
			IBackpackWrapper backpackWrapper = backpackBe.getBackpackWrapper();
			int mainColor = backpackWrapper.getMainColor();
			int accentColor = backpackWrapper.getAccentColor();
			ItemStack backpackCopy = backpackWrapper.getBackpack().copy();
			backpackCopy.remove(ModCoreDataComponents.STORAGE_UUID);
			IBackpackWrapper copyWrapper = BackpackWrapper.fromStack(backpackCopy);
			copyWrapper.setColors(mainColor, accentColor);
			tag.put(BackpackBlockEntity.BACKPACK_DATA_TAG, backpackCopy.save(registries));
		}
	}
}
