package com.remoteandroids.client;

import com.remoteandroids.RemoteAndroids;
import com.remoteandroids.init.ModEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RemoteAndroids.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.ANDROID.get(), AndroidRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.PLAYER_STAND_IN.get(), PlayerStandInRenderer::new);
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(ClientKeyHandler.DISCONNECT_KEY);
		event.register(ClientKeyHandler.REENTER_KEY);
	}
}
