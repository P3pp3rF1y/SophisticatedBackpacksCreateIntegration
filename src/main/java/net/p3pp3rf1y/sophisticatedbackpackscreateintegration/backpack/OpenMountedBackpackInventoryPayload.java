package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;

public class OpenMountedBackpackInventoryPayload implements CustomPacketPayload {
	public static final Type<OpenMountedBackpackInventoryPayload> TYPE = new Type<>(SophisticatedCore.getRL("open_mounted_backpack_inventory"));
	public static final OpenMountedBackpackInventoryPayload INSTANCE = new OpenMountedBackpackInventoryPayload();
	public static final StreamCodec<ByteBuf, OpenMountedBackpackInventoryPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private OpenMountedBackpackInventoryPayload() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(OpenMountedBackpackInventoryPayload payload, IPayloadContext context) {
		Player player = context.player();
		if (player instanceof ServerPlayer serverPlayer)
			if (player.containerMenu instanceof MountedBackpackSettingsContainerMenu menu) {
				MountedSophisticatedBackpack.openMenu(serverPlayer, menu.getContext());
			} else if (player.containerMenu instanceof MountedBackpackContainerMenu menu && !menu.isFirstLevelStorage()) {
				MountedSophisticatedBackpack.openMenu(serverPlayer, menu.getContext().getParentBackpackContext());
			}
	}
}
