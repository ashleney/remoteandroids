package com.remoteandroids.client;

import com.remoteandroids.RemoteAndroids;
import com.remoteandroids.network.DisconnectPacket;
import com.remoteandroids.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RemoteAndroids.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientKeyHandler {

	public static final KeyMapping DISCONNECT_KEY = new KeyMapping("key.remoteandroids.disconnect",
			GLFW.GLFW_KEY_UNKNOWN, "key.remoteandroids.category");

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}
		while (DISCONNECT_KEY.consumeClick()) {
			ModNetwork.CHANNEL.sendToServer(new DisconnectPacket());
		}
	}
}
