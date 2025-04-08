package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;

public record MountedSubBackpackOpenPayload(int slotIndex) implements CustomPacketPayload {
	public static final Type<MountedSubBackpackOpenPayload> TYPE = new Type<>(SophisticatedBackpacks.getRL("mounted_subbackpack_open"));

	public static final StreamCodec<ByteBuf, MountedSubBackpackOpenPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			MountedSubBackpackOpenPayload::slotIndex,
			MountedSubBackpackOpenPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MountedSubBackpackOpenPayload payload, IPayloadContext context) {
		Player player = context.player();

		if (player instanceof ServerPlayer serverPlayer && player.containerMenu instanceof MountedBackpackContainerMenu mountedBackpackContainerMenu) {
			MountedSophisticatedBackpack.openMenu(serverPlayer, mountedBackpackContainerMenu.getContext().getSubBackpackContext(payload.slotIndex()));
		}
	}
}
