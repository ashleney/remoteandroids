package com.remoteandroids.compat;

import com.remoteandroids.data.AndroidSavedData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import javax.annotation.Nullable;

// TODO: We wanted androids to just simply always have 10 hearts and not need to eat/drink
// but instead it just keeps the stats of the player
// we need to somehow clear and restore health while working with nutrition

public class TFCHandler {

	@Nullable
	public static CompoundTag saveFoodData(ServerPlayer player) {
		if (player.getFoodData() instanceof TFCFoodData foodData) {
			return foodData.serializeToPlayerData();
		}
		return null;
	}

	public static void restoreFoodData(ServerPlayer player, @Nullable CompoundTag savedData) {
		if (savedData == null || !(player.getFoodData() instanceof TFCFoodData foodData)) {
			return;
		}
		foodData.deserializeFromPlayerData(savedData);
	}

	public static void resetFoodData(ServerPlayer player) {
		if (!(player.getFoodData() instanceof TFCFoodData foodData)) {
			return;
		}
		foodData.setFoodLevel(20);
		foodData.setSaturation(0.0F);
		foodData.setThirst(TFCFoodData.MAX_THIRST);
		foodData.getNutrition().reset();
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
				|| !(event.player instanceof ServerPlayer player)) {
			return;
		}

		if (!AndroidSavedData.get((ServerLevel) player.level()).isControllingAndroid(player.getUUID())) {
			return;
		}

		if (player.getFoodData() instanceof TFCFoodData foodData) {
			foodData.setThirst(TFCFoodData.MAX_THIRST);
			foodData.setFoodLevel(20);
			foodData.setSaturation(0.0F);
		}
	}
}
