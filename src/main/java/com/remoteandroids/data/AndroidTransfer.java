package com.remoteandroids.data;

import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.PlayerStandInEntity;
import com.remoteandroids.init.ModEntityTypes;
import com.remoteandroids.init.ModItems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** Utility class for swapping an android and a player. */
public final class AndroidTransfer {

	private AndroidTransfer() {
	}

	public enum Result {
		SUCCESS, ALREADY_CONTROLLING, NOT_CONTROLLING, ANDROID_MISSING, ANDROID_BUSY
	}

	private static void forceLoadChunk(ServerLevel level, Vec3 pos) {
		level.getChunk(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
	}

	public static Result swapIn(ServerPlayer player, UUID androidId) {
		MinecraftServer server = player.getServer();
		AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());

		if (data.isControllingAndroid(player.getUUID())) {
			return Result.ALREADY_CONTROLLING;
		}

		AndroidRecord record = data.getAndroid(androidId);
		if (record == null) {
			return Result.ANDROID_MISSING;
		}
		if (!record.idle) {
			return Result.ANDROID_BUSY;
		}

		ServerLevel androidLevel = server.getLevel(record.dimension);
		forceLoadChunk(androidLevel, record.pos());
		Entity found = androidLevel.getEntity(androidId);
		float androidHealth = record.health;
		if (found instanceof AndroidEntity androidEntity) {
			androidHealth = androidEntity.getHealth();
			androidEntity.discard();
		}

		ServerLevel originalLevel = (ServerLevel) player.level();
		Vec3 originalPos = player.position();
		float originalYaw = player.getYRot();
		float originalPitch = player.getXRot();
		GameType originalGameType = player.gameMode.getGameModeForPlayer();
		float originalHealth = player.getHealth();
		double originalMaxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
		ListTag savedInventory = player.getInventory().save(new ListTag());

		UUID standInId = UUID.randomUUID();
		PlayerStandInEntity standIn = new PlayerStandInEntity(ModEntityTypes.PLAYER_STAND_IN.get(), originalLevel);
		standIn.setUUID(standInId);
		standIn.setOwnerUuid(player.getUUID());
		standIn.moveTo(originalPos.x, originalPos.y, originalPos.z, originalYaw, originalPitch);
		standIn.setYHeadRot(originalYaw);
		standIn.setYBodyRot(originalYaw);
		standIn.setYRot(originalYaw);
		standIn.setXRot(originalPitch);
		if (player.getGameProfile() != null) {
			standIn.setCustomName(Component.literal(player.getGameProfile().getName()));
		}
		originalLevel.addFreshEntity(standIn);

		ControlSession session = new ControlSession(player.getUUID(), androidId, standInId, originalLevel.dimension(),
				originalPos, originalYaw, originalPitch, originalGameType, originalHealth, originalMaxHealth,
				savedInventory);
		data.startSession(session);

		data.setAndroidIdle(androidId, false);
		data.updateAndroidState(androidId, androidLevel, record.pos(), record.yaw, record.pitch, androidHealth);

		player.teleportTo(androidLevel, record.x, record.y, record.z, record.yaw, record.pitch);
		fillAndroidInventory(player);
		player.setGameMode(GameType.ADVENTURE);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
		player.setHealth(Math.max(1.0F, Math.min(20.0F, androidHealth)));
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(0.0F);

		return Result.SUCCESS;
	}

	public static void fillAndroidInventory(ServerPlayer player) {
		var inventory = player.getInventory();
		ItemStack lock = new ItemStack(ModItems.INVENTORY_LOCK.get());
		inventory.clearContent();
		inventory.setItem(0, new ItemStack(ModItems.RECALL_CORE.get()));
		for (int slot = 1; slot < inventory.items.size(); slot++) {
			inventory.setItem(slot, lock.copy());
		}
		for (int slot = 0; slot < inventory.armor.size(); slot++) {
			inventory.armor.set(slot, lock.copy());
		}
		for (int slot = 0; slot < inventory.offhand.size(); slot++) {
			inventory.offhand.set(slot, lock.copy());
		}
	}

	public static Result swapOut(ServerPlayer player) {
		return swapOut(player, true);
	}

	public static Result swapOut(ServerPlayer player, boolean respawnAndroid) {
		MinecraftServer server = player.getServer();
		if (server == null)
			return Result.NOT_CONTROLLING;

		AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());
		ControlSession session = data.getSession(player.getUUID());
		if (session == null) {
			return Result.NOT_CONTROLLING;
		}

		ServerLevel originalLevel = server.getLevel(session.originalDimension);
		forceLoadChunk(originalLevel, session.pos());
		Entity standIn = originalLevel.getEntity(session.standInId);
		if (standIn != null) {
			standIn.discard();
		}

		AndroidRecord savedAndroid = data.getAndroid(session.androidId);
		ServerLevel currentLevel = savedAndroid == null
				? (ServerLevel) player.level()
				: server.getLevel(savedAndroid.dimension);
		Vec3 currentPos = savedAndroid == null ? player.position() : savedAndroid.pos();
		float currentYaw = savedAndroid == null ? player.getYHeadRot() : savedAndroid.yaw;
		float currentPitch = savedAndroid == null ? player.getXRot() : savedAndroid.pitch;

		if (respawnAndroid) {
			AndroidEntity android = new AndroidEntity(ModEntityTypes.ANDROID.get(), currentLevel);
			android.setUUID(session.androidId);
			android.moveTo(currentPos.x, currentPos.y, currentPos.z, currentYaw, currentPitch);
			android.setYHeadRot(currentYaw);
			android.setYBodyRot(currentYaw);
			android.setYRot(currentYaw);
			android.setXRot(currentPitch);
			if (savedAndroid != null) {
				android.setHealth(Math.max(1.0F, Math.min(android.getMaxHealth(), savedAndroid.health)));
			}
			currentLevel.addFreshEntity(android);

			data.setAndroidIdle(session.androidId, true);
			data.updateAndroidState(session.androidId, currentLevel, currentPos, currentYaw, currentPitch,
					android.getHealth());
		} else {
			data.removeAndroid(session.androidId);
		}

		player.teleportTo(originalLevel, session.x, session.y, session.z, session.yaw, session.pitch);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(session.originalMaxHealth);
		player.setHealth(Math.max(1.0F, Math.min(player.getMaxHealth(), session.originalHealth)));
		player.getInventory().clearContent();
		player.getInventory().load(session.inventory);
		player.setGameMode(session.originalGameType);

		data.endSession(player.getUUID());

		return Result.SUCCESS;
	}
}
