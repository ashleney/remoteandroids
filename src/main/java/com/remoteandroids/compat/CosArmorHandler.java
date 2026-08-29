package com.remoteandroids.compat;

import javax.annotation.Nullable;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CosArmorHandler {

	private static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
			EquipmentSlot.HEAD};

	private CosArmorHandler() {
	}

	@Nullable
	public static CompoundTag snapshot(ServerPlayer player) {
		CAStacksBase source = CosArmorAPI.getCAStacks(player.getUUID());
		CAStacksBase snapshot = new CAStacksBase(4);
		boolean any = false;
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack stack = source.getStackInSlot(i);
			boolean skinArmor = source.isSkinArmor(i);
			any |= !stack.isEmpty() || skinArmor;
			snapshot.setStackInSlot(i, stack.copy());
			snapshot.setSkinArmor(i, skinArmor);
		}
		return any ? snapshot.serializeNBT() : null;
	}

	public static void apply(LivingEntity body, @Nullable CompoundTag cosArmor) {
		if (cosArmor == null) {
			return;
		}
		CAStacksBase stacks = new CAStacksBase(4);
		stacks.deserializeNBT(cosArmor);
		for (int i = 0; i < SLOTS.length; i++) {
			if (stacks.isSkinArmor(i)) {
				body.setItemSlot(SLOTS[i], ItemStack.EMPTY);
			} else {
				ItemStack cosmetic = stacks.getStackInSlot(i);
				if (!cosmetic.isEmpty()) {
					body.setItemSlot(SLOTS[i], cosmetic.copy());
				}
			}
		}
	}
}
