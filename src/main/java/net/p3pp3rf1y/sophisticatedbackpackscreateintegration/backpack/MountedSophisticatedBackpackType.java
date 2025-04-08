package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.jetbrains.annotations.Nullable;

public class MountedSophisticatedBackpackType extends MountedItemStorageType<MountedSophisticatedBackpack> {
	public MountedSophisticatedBackpackType() {
		super(MountedSophisticatedBackpack.CODEC);
	}

	@Override
	public @Nullable MountedSophisticatedBackpack mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof BackpackBlockEntity backpackBe ? MountedSophisticatedBackpack.from(backpackBe) : null;
	}
}
