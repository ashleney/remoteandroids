package com.remoteandroids.network;

import com.remoteandroids.RemoteAndroids;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(RemoteAndroids.MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	private static int id = 0;

	public static void register() {
		CHANNEL.registerMessage(id++, DisconnectPacket.class, DisconnectPacket::encode, DisconnectPacket::decode,
				DisconnectPacket::handle);
	}
}
