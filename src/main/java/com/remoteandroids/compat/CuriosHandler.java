package com.remoteandroids.compat;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosHandler {

	private CuriosHandler() {
	}

	public static ListTag saveAndClear(LivingEntity entity) {
		return CuriosApi.getCuriosInventory(entity).map(handler -> handler.saveInventory(true)).orElse(null);
	}

	public static ListTag saveSnapshot(LivingEntity entity) {
		return CuriosApi.getCuriosInventory(entity).map(handler -> handler.saveInventory(false)).orElse(null);
	}

	public static void restore(LivingEntity entity, ListTag savedData) {
		if (savedData == null)
			return;
		CuriosApi.getCuriosInventory(entity).ifPresent(handler -> handler.loadInventory(savedData));
	}

	public static void clear(LivingEntity entity) {
		CuriosApi.getCuriosInventory(entity).ifPresent(handler -> handler.saveInventory(true));
	}
}
