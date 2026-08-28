package com.remoteandroids;

import com.remoteandroids.compat.ModCompat;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.PlayerStandInEntity;
import com.remoteandroids.event.ModEventHandlers;
import com.remoteandroids.init.ModCreativeTabs;
import com.remoteandroids.init.ModEntityTypes;
import com.remoteandroids.init.ModItems;
import com.remoteandroids.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RemoteAndroids.MOD_ID)
public class RemoteAndroids {

	public static final String MOD_ID = "remoteandroids";

	public RemoteAndroids() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		ModItems.ITEMS.register(modBus);
		ModEntityTypes.ENTITY_TYPES.register(modBus);
		ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);

		ModNetwork.register();

		modBus.addListener(this::registerAttributes);

		MinecraftForge.EVENT_BUS.register(new ModEventHandlers());

		ModCompat.register();
	}

	private void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(ModEntityTypes.ANDROID.get(), AndroidEntity.createAttributes().build());
		event.put(ModEntityTypes.PLAYER_STAND_IN.get(), PlayerStandInEntity.createAttributes().build());
	}
}
