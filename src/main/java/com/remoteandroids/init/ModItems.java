package com.remoteandroids.init;

import com.remoteandroids.RemoteAndroids;
import com.remoteandroids.entity.AndroidEntity.AndroidType;
import com.remoteandroids.item.AndroidCoreItem;
import com.remoteandroids.item.DisconnectItem;
import com.remoteandroids.item.InventoryLockItem;
import com.remoteandroids.item.NeuralRemoteItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			RemoteAndroids.MOD_ID);

	public static final RegistryObject<Item> OBSERVER_CORE = ITEMS.register("observer_core",
			() -> new AndroidCoreItem(new Item.Properties().stacksTo(16), AndroidType.OBSERVER));

	public static final RegistryObject<Item> ADVENTURE_CORE = ITEMS.register("adventure_core",
			() -> new AndroidCoreItem(new Item.Properties().stacksTo(16), AndroidType.ADVENTURE));

	public static final RegistryObject<Item> SURVIVAL_CORE = ITEMS.register("survival_core",
			() -> new AndroidCoreItem(new Item.Properties().stacksTo(16), AndroidType.SURVIVAL));

	public static final RegistryObject<Item> NEURAL_REMOTE = ITEMS.register("neural_remote",
			() -> new NeuralRemoteItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> DISCONNECT = ITEMS.register("disconnect",
			() -> new DisconnectItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> INVENTORY_LOCK = ITEMS.register("inventory_lock",
			() -> new InventoryLockItem(new Item.Properties().stacksTo(64)));
}
