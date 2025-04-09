package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record MountedSubBackpackOpenMessage(int slotIndex) {

	public static void encode(MountedSubBackpackOpenMessage msg, ByteBuf buffer) {
		buffer.writeInt(msg.slotIndex());
	}

	public static MountedSubBackpackOpenMessage decode(ByteBuf buffer) {
		return new MountedSubBackpackOpenMessage(buffer.readInt());
	}

	public static void onMessage(MountedSubBackpackOpenMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(context.getSender(), msg));
		context.setPacketHandled(true);
	}
	public static void handleMessage(@Nullable ServerPlayer player, MountedSubBackpackOpenMessage msg) {
		if (player == null) {
			return;
		}

		if (player.containerMenu instanceof MountedBackpackContainerMenu mountedBackpackContainerMenu) {
			MountedSophisticatedBackpack.openMenu(player, mountedBackpackContainerMenu.getContext().getSubBackpackContext(msg.slotIndex()));
		}
	}
}
