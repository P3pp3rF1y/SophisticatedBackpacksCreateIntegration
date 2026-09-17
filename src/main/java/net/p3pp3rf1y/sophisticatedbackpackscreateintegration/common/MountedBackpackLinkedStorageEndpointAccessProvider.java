package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageEndpointAccessProvider;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointStackState;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupManager;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackLifecycle;

import java.util.UUID;

public class MountedBackpackLinkedStorageEndpointAccessProvider implements ILinkedStorageEndpointAccessProvider {
	@Override
	public boolean hasGroupEndpoint(ServerPlayer player, LinkedStorageGroupManager manager, UUID groupId) {
		if (!(player.containerMenu instanceof MountedBackpackContainerMenu menu)) {
			return false;
		}

		IStorageWrapper storageWrapper = menu.getStorageWrapper();
		if (menu.getContext().getBackpackWrapper(player) != storageWrapper) {
			return false;
		}

		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		for (int slot = 0; slot < inventory.size(); slot++) {
			if (isGroupEndpoint(inventory.getStackInSlot(slot), manager, groupId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isGroupEndpoint(ItemStack stack, LinkedStorageGroupManager manager, UUID groupId) {
		LinkedStorageEndpointData endpoint = stack.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		return endpoint != null && LinkedStorageStackLifecycle.classifyEndpoint(stack) == LinkedStorageEndpointStackState.ENDPOINT
				&& endpoint.groupId().equals(groupId) && manager.isEndpointMember(groupId, endpoint.endpointId());
	}
}
