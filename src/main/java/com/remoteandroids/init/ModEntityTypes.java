package com.remoteandroids.init;

import com.remoteandroids.RemoteAndroids;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.PlayerStandInEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
			.create(ForgeRegistries.ENTITY_TYPES, RemoteAndroids.MOD_ID);

	public static final RegistryObject<EntityType<AndroidEntity>> ANDROID = ENTITY_TYPES.register("android",
			() -> EntityType.Builder.of(AndroidEntity::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10)
					.build("android"));

	public static final RegistryObject<EntityType<PlayerStandInEntity>> PLAYER_STAND_IN = ENTITY_TYPES
			.register("player_stand_in", () -> EntityType.Builder.of(PlayerStandInEntity::new, MobCategory.MISC)
					.sized(0.6F, 1.95F).clientTrackingRange(10).build("player_stand_in"));
}
