package com.remoteandroids.network;

import com.remoteandroids.client.ClientControlHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ControlStatePacket {

	private final boolean controlling;

	public ControlStatePacket(boolean controlling) {
		this.controlling = controlling;
	}

	public static void encode(ControlStatePacket msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.controlling);
	}

	public static ControlStatePacket decode(FriendlyByteBuf buf) {
		return new ControlStatePacket(buf.readBoolean());
	}

	public static void handle(ControlStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getDirection().getReceptionSide().isClient()) {
				ClientControlHandler.setControlling(msg.controlling);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
