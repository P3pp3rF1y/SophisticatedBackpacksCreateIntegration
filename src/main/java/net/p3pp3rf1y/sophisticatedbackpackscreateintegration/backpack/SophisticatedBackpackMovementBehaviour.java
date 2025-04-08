package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import org.jetbrains.annotations.Nullable;

public class SophisticatedBackpackMovementBehaviour implements MovementBehaviour {
	public static final SophisticatedBackpackMovementBehaviour INSTANCE = new SophisticatedBackpackMovementBehaviour();

	private SophisticatedBackpackMovementBehaviour() {
	}

	@Override
	public void tick(MovementContext context) {
		@Nullable MountedSophisticatedBackpack storage = getMountedSophisticatedBackpack(context);
		if (storage != null) {
			storage.initEntityLevelAndPositions(context);
			storage.clearNbt();
			storage.tick();
		}
	}

	//TODO replace with direct call to context.getItemStorage once this is fixed for when storage is synced and new instance is created on client
	@Nullable
	private MountedSophisticatedBackpack getMountedSophisticatedBackpack(MovementContext context) {
		if (ContraptionHelper.getMountedStorage(context.contraption.entity, context.localPos) instanceof MountedSophisticatedBackpack mountedSophisticatedStorage) {
			return mountedSophisticatedStorage;
		}

		return null;
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		@Nullable MountedSophisticatedBackpack storage = getMountedSophisticatedBackpack(context);
		if (storage != null) {
			storage.setPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
		}
	}
}
