package com.remoteandroids.network;

import com.remoteandroids.data.AndroidRecord;
import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.data.AndroidTransfer;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ReenterPacket {

	public ReenterPacket() {
	}

	public static void encode(ReenterPacket msg, FriendlyByteBuf buf) {
	}

	public static ReenterPacket decode(FriendlyByteBuf buf) {
		return new ReenterPacket();
	}

	public static void handle(ReenterPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) {
				return;
			}
			AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());
			if (data.isControllingAndroid(player.getUUID())) {
				AndroidTransfer.describe(player, AndroidTransfer.Result.ALREADY_CONTROLLING);
				return;
			}
			UUID androidId = data.getLastAndroid(player.getUUID());
			if (androidId == null) {
				player.displayClientMessage(
						Component.translatable("remoteandroids.msg.no_last_android").withStyle(ChatFormatting.RED),
						true);
				return;
			}
			AndroidRecord record = data.getAndroid(androidId);
			if (record == null) {
				AndroidTransfer.describe(player, AndroidTransfer.Result.ANDROID_MISSING);
				return;
			}
			if (!record.idle) {
				AndroidTransfer.describe(player, AndroidTransfer.Result.ANDROID_BUSY);
				return;
			}
			AndroidTransfer.describe(player, AndroidTransfer.swapIn(player, androidId));
		});
		ctx.get().setPacketHandled(true);
	}
}
