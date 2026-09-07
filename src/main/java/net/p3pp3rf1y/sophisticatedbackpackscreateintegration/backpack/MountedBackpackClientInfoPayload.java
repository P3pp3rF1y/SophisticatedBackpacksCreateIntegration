package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;

public record MountedBackpackClientInfoPayload(int contraptionEntityId, BlockPos localPos, CompoundTag renderInfoNbt,
		int columnsTaken) implements CustomPacketPayload {
	public static final Type<MountedBackpackClientInfoPayload> TYPE = new Type<>(SophisticatedBackpacksCreateIntegration.getRL("mounted_backpack_client_info"));
	public static final StreamCodec<ByteBuf, MountedBackpackClientInfoPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT,
			MountedBackpackClientInfoPayload::contraptionEntityId, BlockPos.STREAM_CODEC, MountedBackpackClientInfoPayload::localPos,
			ByteBufCodecs.COMPOUND_TAG, MountedBackpackClientInfoPayload::renderInfoNbt, ByteBufCodecs.INT, MountedBackpackClientInfoPayload::columnsTaken,
			MountedBackpackClientInfoPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MountedBackpackClientInfoPayload payload, IPayloadContext context) {
		Player player = context.player();
		if (player.containerMenu instanceof MountedBackpackContainerMenu menu && menu.getContext().getContraptionEntityId() == payload.contraptionEntityId
				&& menu.getContext().getLocalPos().equals(payload.localPos)) {
			menu.syncClientProfile(payload.renderInfoNbt, payload.columnsTaken);
		}
	}
}
