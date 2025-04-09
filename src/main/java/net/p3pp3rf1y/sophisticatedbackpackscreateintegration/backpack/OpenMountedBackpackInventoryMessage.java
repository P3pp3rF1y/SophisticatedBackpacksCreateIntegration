package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class OpenMountedBackpackInventoryMessage {
	public static final OpenMountedBackpackInventoryMessage INSTANCE = new OpenMountedBackpackInventoryMessage();

	private OpenMountedBackpackInventoryMessage() {
	}

	public static void encode(OpenMountedBackpackInventoryMessage msg, FriendlyByteBuf buffer) {
		//noop
	}

	public static OpenMountedBackpackInventoryMessage decode(FriendlyByteBuf buffer) {
		return INSTANCE;
	}

	public static void onMessage(OpenMountedBackpackInventoryMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(context.getSender()));
		context.setPacketHandled(true);
	}

	public static void handleMessage(@Nullable ServerPlayer player) {
		if (player == null) {
			return;
		}

		if (player.containerMenu instanceof MountedBackpackSettingsContainerMenu menu) {
			MountedSophisticatedBackpack.openMenu(player, menu.getContext());
		} else if (player.containerMenu instanceof MountedBackpackContainerMenu menu && !menu.isFirstLevelStorage()) {
			MountedSophisticatedBackpack.openMenu(player, menu.getContext().getParentBackpackContext());
		}
	}
}
