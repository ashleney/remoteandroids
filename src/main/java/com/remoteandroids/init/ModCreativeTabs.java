package com.remoteandroids.init;

import com.remoteandroids.RemoteAndroids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, RemoteAndroids.MOD_ID);

	public static final RegistryObject<CreativeModeTab> REMOTE_ANDROIDS = CREATIVE_MODE_TABS.register("main",
			() -> CreativeModeTab.builder().title(Component.translatable("itemGroup.remoteandroids"))
					.icon(() -> new ItemStack(ModItems.ANDROID_CORE.get())).displayItems((parameters, output) -> {
						output.accept(ModItems.ANDROID_CORE.get());
						output.accept(ModItems.NEURAL_REMOTE.get());
						output.accept(ModItems.RECALL_CORE.get());
					}).build());

	private ModCreativeTabs() {
	}
}
