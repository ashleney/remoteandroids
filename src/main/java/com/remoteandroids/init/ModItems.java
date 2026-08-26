package com.remoteandroids.init;

import com.remoteandroids.RemoteAndroids;
import com.remoteandroids.item.AndroidCoreItem;
import com.remoteandroids.item.InventoryLockItem;
import com.remoteandroids.item.NeuralRemoteItem;
import com.remoteandroids.item.RecallCoreItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			RemoteAndroids.MOD_ID);

	public static final RegistryObject<Item> ANDROID_CORE = ITEMS.register("android_core",
			() -> new AndroidCoreItem(new Item.Properties().stacksTo(16)));

	public static final RegistryObject<Item> NEURAL_REMOTE = ITEMS.register("neural_remote",
			() -> new NeuralRemoteItem(new Item.Properties().stacksTo(1).durability(0)));

	public static final RegistryObject<Item> RECALL_CORE = ITEMS.register("recall_core",
			() -> new RecallCoreItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> INVENTORY_LOCK = ITEMS.register("inventory_lock",
			() -> new InventoryLockItem(new Item.Properties().stacksTo(64)));
}
