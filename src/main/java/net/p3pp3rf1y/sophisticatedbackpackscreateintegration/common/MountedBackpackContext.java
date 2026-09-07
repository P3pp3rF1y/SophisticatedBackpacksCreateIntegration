package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackLinkedStorageResolver;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.LinkedStorageBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack.MountedSophisticatedBackpack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.ContraptionHelper;
import net.p3pp3rf1y.sophisticatedcore.compat.create.MountedStorageBase;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
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

		if (itemStorage instanceof MountedSophisticatedBackpack mountedBackpack) {
			mountedBackpack.setLevel(player.level());
			return mountedBackpack.getStorageWrapperForMenu();
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
		buffer.writeBoolean(false);
	}

	public void toBuffer(FriendlyByteBuf buffer, Player player) {
		getType().toBuffer(buffer);
		addToBuffer(buffer);
		IStorageWrapper storageWrapper = getBackpackWrapper(player);
		if (storageWrapper instanceof IBackpackWrapper backpackWrapper) {
			BackpackContext.writeLinkedStorageSnapshot(buffer, player, backpackWrapper);
		} else {
			buffer.writeBoolean(false);
		}
	}

	public void addToBuffer(FriendlyByteBuf buffer) {
		buffer.writeInt(contraptionEntityId);
		buffer.writeBlockPos(localPos);
	}

	public static MountedBackpackContext fromBuffer(FriendlyByteBuf buffer) {
		BackpackContext.ContextType type = BackpackContext.ContextType.fromBuffer(buffer);
		MountedBackpackContext context;
		if (type == BackpackContext.ContextType.ITEM_SUB_BACKPACK) {
			context = SubBackpack.fromBuffer(buffer);
		} else if (type == BackpackContext.ContextType.ITEM_BACKPACK) {
			context = new MountedBackpackContext(buffer.readInt(), buffer.readBlockPos());
		} else {
			throw new IllegalArgumentException();
		}
		BackpackContext.readLinkedStorageSnapshot(buffer);
		return context;
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

	public void close() {
		// The main wrapper belongs to the mounted storage and outlives individual menus.
	}

	public static class SubBackpack extends MountedBackpackContext {
		private final int subBackpackSlotIndex;
		@Nullable
		private IStorageWrapper parentWrapper;
		@Nullable
		private IBackpackWrapper backpackWrapper;

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
			return getParentBackpackWrapper(player).map(parent -> {
				ItemStack stackInSlot = parent.getInventoryHandler().getStackInSlot(subBackpackSlotIndex);
				if (!(stackInSlot.getItem() instanceof BackpackItem)) {
					closeBackpackWrapper();
					return IBackpackWrapper.Noop.INSTANCE;
				}
				if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper
						&& linkedStorageBackpackWrapper.hasEndpoint(LinkedStorageStackData.getEndpoint(stackInSlot))) {
					linkedStorageBackpackWrapper.replacePhysicalBackpackStack(stackInSlot);
				} else if (backpackWrapper == null || backpackWrapper.getBackpack() != stackInSlot || backpackWrapper instanceof LinkedStorageBackpackWrapper) {
					closeBackpackWrapper();
					backpackWrapper = BackpackLinkedStorageResolver.resolveOrCreate(player.level(), stackInSlot);
				} else if (LinkedStorageStackData.getEndpoint(stackInSlot) != null) {
					BackpackLinkedStorageResolver.resolve(player.level(), stackInSlot).ifPresent(linkedStorageBackpackWrapper -> {
						closeBackpackWrapper();
						backpackWrapper = linkedStorageBackpackWrapper;
					});
				}
				if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper) {
					linkedStorageBackpackWrapper.setCanonicalContentsChangedHandler(this::saveBackpackStack);
				}
				return backpackWrapper;
			}).orElse(IBackpackWrapper.Noop.INSTANCE);
		}

		@Override
		public void close() {
			closeBackpackWrapper();
		}

		private void closeBackpackWrapper() {
			if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper) {
				linkedStorageBackpackWrapper.close();
			}
			backpackWrapper = null;
		}

		private void saveBackpackStack() {
			if (parentWrapper != null && backpackWrapper != null && isCurrentSubBackpackStack()) {
				parentWrapper.getInventoryHandler().setStackInSlot(subBackpackSlotIndex, ItemStack.EMPTY);
				parentWrapper.getInventoryHandler().setStackInSlot(subBackpackSlotIndex, backpackWrapper.getBackpack());
			}
		}

		private boolean isCurrentSubBackpackStack() {
			ItemStack currentStack = parentWrapper.getInventoryHandler().getStackInSlot(subBackpackSlotIndex);
			if (backpackWrapper instanceof LinkedStorageBackpackWrapper linkedStorageBackpackWrapper) {
				return linkedStorageBackpackWrapper.hasEndpoint(LinkedStorageStackData.getEndpoint(currentStack));
			}
			return backpackWrapper.getBackpack() == currentStack;
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
