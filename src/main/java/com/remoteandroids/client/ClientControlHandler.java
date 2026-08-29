package com.remoteandroids.client;

import com.remoteandroids.compat.Mods;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientControlHandler {

	private static final int COS_SLOTS = 4;

	private static boolean controlling;
	private static boolean cosArmorSaved;
	private static final ItemStack[] savedCosArmor = new ItemStack[COS_SLOTS];
	private static final boolean[] savedSkinArmor = new boolean[COS_SLOTS];

	private ClientControlHandler() {
	}

	public static boolean isControlling() {
		return controlling;
	}

	public static void setControlling(boolean value) {
		if (controlling == value) {
			return;
		}
		controlling = value;
		if (!Mods.COSMETIC_ARMOR.isLoaded() || Minecraft.getInstance().player == null) {
			return;
		}
		CAStacksBase cos = CosArmorAPI.getCAStacksClient(Minecraft.getInstance().player.getUUID());
		if (value) {
			for (int i = 0; i < COS_SLOTS; i++) {
				savedCosArmor[i] = cos.getStackInSlot(i).copy();
				savedSkinArmor[i] = cos.isSkinArmor(i);
			}
			for (int i = 0; i < COS_SLOTS; i++) {
				cos.setStackInSlot(i, ItemStack.EMPTY);
				cos.setSkinArmor(i, false);
			}
			cosArmorSaved = true;
		} else if (cosArmorSaved) {
			for (int i = 0; i < COS_SLOTS; i++) {
				cos.setStackInSlot(i, savedCosArmor[i]);
				cos.setSkinArmor(i, savedSkinArmor[i]);
			}
			cosArmorSaved = false;
		}
	}
}
