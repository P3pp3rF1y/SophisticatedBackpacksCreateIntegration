package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSophisticatedBackpack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

import javax.annotation.Nullable;
import java.util.Optional;

public class MountedBackpackContext {
	private final int contraptionEntityId;
	private final BlockPos localPos;

	public MountedBackpackContext(int contraptionEntityId, BlockPos localPos) {
		this.contraptionEntityId = contraptionEntityId;
		this.localPos = localPos;
	}

	public Optional<IStorageWrapper> getParentBackpackWrapper(Player player) {
		return Optional.empty();
	}

	public IStorageWrapper getBackpackWrapper(Player player) {
		if (!(player.level().getEntity(contraptionEntityId) instanceof AbstractContraptionEntity contraptionEntity)) {
			return NoopStorageWrapper.INSTANCE;
		}
		MountedStorageBase itemStorage = ContraptionHelper.getMountedStorage(contraptionEntity, localPos);
		if (itemStorage == null) {
			return NoopStorageWrapper.INSTANCE;
		}

		return itemStorage.getStorageWrapper();
	}

	public MountedBackpackContext getSubBackpackContext(int subBackpackSlotIndex) {
		return new MountedBackpackContext.SubBackpack(contraptionEntityId, localPos, subBackpackSlotIndex);
	}

	public MountedBackpackContext getParentBackpackContext() {
		return this;
	}

	public int getContraptionEntityId() {
		return contraptionEntityId;
	}

	public BlockPos getLocalPos() {
		return localPos;
	}

	public void toBuffer(FriendlyByteBuf buffer) {
		getType().toBuffer(buffer);
		addToBuffer(buffer);
	}

	public void addToBuffer(FriendlyByteBuf buffer){
		buffer.writeInt(contraptionEntityId);
		buffer.writeBlockPos(localPos);
	}

	public static MountedBackpackContext fromBuffer(FriendlyByteBuf buffer) {
		BackpackContext.ContextType type = BackpackContext.ContextType.fromBuffer(buffer);
		if (type == BackpackContext.ContextType.ITEM_SUB_BACKPACK) {
			return SubBackpack.fromBuffer(buffer);
		} else if (type == BackpackContext.ContextType.ITEM_BACKPACK) {
			return new MountedBackpackContext(buffer.readInt(), buffer.readBlockPos());
		}
		throw new IllegalArgumentException();
	}

	public BackpackContext.ContextType getType() {
		return BackpackContext.ContextType.ITEM_BACKPACK;
	}

	public Component getDisplayName(ServerPlayer player) {
		return getBackpackWrapper(player).getDisplayName();
	}

	public void setBlockRenderDirty(Player player) {
		if (!(player.level().getEntity(contraptionEntityId) instanceof AbstractContraptionEntity contraptionEntity)) {
			return;
		}
		MountedStorageBase mountedStorage = ContraptionHelper.getMountedStorage(contraptionEntity, localPos);
		if (mountedStorage instanceof MountedSophisticatedBackpack mountedSophisticatedBackpack) {
			mountedSophisticatedBackpack.setBlockRenderDirty();
		}
	}

	public static class SubBackpack extends MountedBackpackContext {
		private final int subBackpackSlotIndex;
		@Nullable
		private IStorageWrapper parentWrapper;

		public SubBackpack(int contraptionEntityId, BlockPos localPos, int subBackpackSlotIndex) {
			super(contraptionEntityId, localPos);
			this.subBackpackSlotIndex = subBackpackSlotIndex;
		}

		@Override
		public Optional<IStorageWrapper> getParentBackpackWrapper(Player player) {
			if (parentWrapper == null) {
				parentWrapper = super.getBackpackWrapper(player);
			}
			return Optional.of(parentWrapper);
		}

		@Override
		public IStorageWrapper getBackpackWrapper(Player player) {
			return getParentBackpackWrapper(player)
					.map(parent -> {
						ItemStack stackInSlot = parent.getInventoryHandler().getStackInSlot(subBackpackSlotIndex);
						if (!(stackInSlot.getItem() instanceof BackpackItem)) {
							return IBackpackWrapper.Noop.INSTANCE;
						}
						return BackpackWrapper.fromStack(stackInSlot);
					}).orElse(IBackpackWrapper.Noop.INSTANCE);
		}

		@Override
		public void addToBuffer(FriendlyByteBuf buffer) {
			super.addToBuffer(buffer);
			buffer.writeInt(subBackpackSlotIndex);
		}

		public static MountedBackpackContext fromBuffer(FriendlyByteBuf buffer) {
			return new MountedBackpackContext.SubBackpack(buffer.readInt(), buffer.readBlockPos(), buffer.readInt());
		}

		@Override
		public MountedBackpackContext getParentBackpackContext() {
			return new MountedBackpackContext(getContraptionEntityId(), getLocalPos());
		}

		@Override
		public BackpackContext.ContextType getType() {
			return BackpackContext.ContextType.ITEM_SUB_BACKPACK;
		}

		@Override
		public Component getDisplayName(ServerPlayer player) {
			return Component.literal(BackpackContext.SUBBACKPACK_DISPLAY_NAME_PREFIX + super.getDisplayName(player).getString());
		}
	}
}
