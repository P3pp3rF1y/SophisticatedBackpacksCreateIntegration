package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;

import java.util.function.Supplier;

public record MountedBackpackClientInfoMessage(int contraptionEntityId, BlockPos localPos, CompoundTag renderInfoNbt, int columnsTaken) {
	public static void encode(MountedBackpackClientInfoMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.contraptionEntityId);
		buffer.writeBlockPos(message.localPos);
		buffer.writeNbt(message.renderInfoNbt);
		buffer.writeInt(message.columnsTaken);
	}

	public static MountedBackpackClientInfoMessage decode(FriendlyByteBuf buffer) {
		return new MountedBackpackClientInfoMessage(buffer.readInt(), buffer.readBlockPos(), buffer.readAnySizeNbt(), buffer.readInt());
	}

	public static void onMessage(MountedBackpackClientInfoMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().player != null && message.renderInfoNbt != null
					&& Minecraft.getInstance().player.containerMenu instanceof MountedBackpackContainerMenu menu
					&& menu.getContext().getContraptionEntityId() == message.contraptionEntityId && menu.getContext().getLocalPos().equals(message.localPos)) {
				menu.syncClientProfile(message.renderInfoNbt, message.columnsTaken);
			}
		});
		context.setPacketHandled(true);
	}
}
