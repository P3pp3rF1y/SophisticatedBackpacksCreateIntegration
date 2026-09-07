package net.p3pp3rf1y.sophisticatedbackpackscreateintegration.backpack;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.SophisticatedBackpacksCreateIntegration;
import net.p3pp3rf1y.sophisticatedbackpackscreateintegration.common.MountedBackpackContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderData;

public record MountedBackpackClientInfoPayload(int contraptionEntityId, BlockPos localPos, RenderData renderData,
		int columnsTaken) implements CustomPacketPayload {
	public static final Type<MountedBackpackClientInfoPayload> TYPE = new Type<>(SophisticatedBackpacksCreateIntegration.getRL("mounted_backpack_client_info"));
	private static final StreamCodec<ByteBuf, Integer> COLUMNS_TAKEN_STREAM_CODEC = ByteBufCodecs.INT;
	public static final StreamCodec<RegistryFriendlyByteBuf, MountedBackpackClientInfoPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT,
			MountedBackpackClientInfoPayload::contraptionEntityId, BlockPos.STREAM_CODEC, MountedBackpackClientInfoPayload::localPos, RenderData.STREAM_CODEC,
			MountedBackpackClientInfoPayload::renderData, COLUMNS_TAKEN_STREAM_CODEC, MountedBackpackClientInfoPayload::columnsTaken,
			MountedBackpackClientInfoPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MountedBackpackClientInfoPayload payload, IPayloadContext context) {
		Player player = context.player();
		if (player.containerMenu instanceof MountedBackpackContainerMenu menu && menu.getContext().getContraptionEntityId() == payload.contraptionEntityId
				&& menu.getContext().getLocalPos().equals(payload.localPos)) {
			menu.syncClientProfile(payload.renderData, payload.columnsTaken);
		}
	}
}
