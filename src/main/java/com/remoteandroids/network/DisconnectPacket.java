package com.remoteandroids.network;

import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.data.AndroidTransfer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DisconnectPacket {

	public DisconnectPacket() {
	}

	public static void encode(DisconnectPacket msg, FriendlyByteBuf buf) {
	}

	public static DisconnectPacket decode(FriendlyByteBuf buf) {
		return new DisconnectPacket();
	}

	public static void handle(DisconnectPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				AndroidSavedData data = AndroidSavedData.get((net.minecraft.server.level.ServerLevel) player.level());
				if (data.isControllingAndroid(player.getUUID())) {
					AndroidTransfer.Result result = AndroidTransfer.swapOut(player);
					if (result == AndroidTransfer.Result.SUCCESS) {
						player.displayClientMessage(
								net.minecraft.network.chat.Component.translatable("remoteandroids.msg.welcome_back")
										.withStyle(net.minecraft.ChatFormatting.AQUA),
								true);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
