package com.remoteandroids.data;

import com.remoteandroids.compat.CosArmorHandler;
import com.remoteandroids.compat.CuriosHandler;
import com.remoteandroids.compat.Mods;
import com.remoteandroids.compat.TFCHandler;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.AndroidEntity.AndroidType;
import com.remoteandroids.entity.PlayerStandInEntity;
import com.remoteandroids.init.ModEntityTypes;
import com.remoteandroids.init.ModItems;
import com.remoteandroids.network.ControlStatePacket;
import com.remoteandroids.network.ModNetwork;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/** Utility class for swapping an android and a player. */
public final class AndroidTransfer {

	private AndroidTransfer() {
	}

	public enum Result {
		SUCCESS, ALREADY_CONTROLLING, NOT_CONTROLLING, ANDROID_MISSING, ANDROID_BUSY
	}

	/** Shows the outcome of a transfer attempt to the player. */
	public static void describe(ServerPlayer player, Result result) {
		switch (result) {
			case SUCCESS -> player.displayClientMessage(Component.translatable("remoteandroids.msg.transferred")
					.withStyle(net.minecraft.ChatFormatting.AQUA), true);
			case ALREADY_CONTROLLING ->
				player.displayClientMessage(Component.translatable("remoteandroids.msg.already_controlling")
						.withStyle(net.minecraft.ChatFormatting.RED), true);
			case ANDROID_BUSY -> player.displayClientMessage(Component.translatable("remoteandroids.msg.android_busy")
					.withStyle(net.minecraft.ChatFormatting.RED), true);
			case ANDROID_MISSING ->
				player.displayClientMessage(Component.translatable("remoteandroids.msg.android_missing")
						.withStyle(net.minecraft.ChatFormatting.RED), true);
			default -> {
			}
		}
	}

	private static void forceLoadChunk(ServerLevel level, Vec3 pos) {
		level.getChunk(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
	}

	/** Mirrors the player's armor and held items onto a body entity for display. */
	private static void equipBody(ServerPlayer player, LivingEntity body) {
		body.setItemSlot(EquipmentSlot.FEET, player.getInventory().armor.get(0).copy());
		body.setItemSlot(EquipmentSlot.LEGS, player.getInventory().armor.get(1).copy());
		body.setItemSlot(EquipmentSlot.CHEST, player.getInventory().armor.get(2).copy());
		body.setItemSlot(EquipmentSlot.HEAD, player.getInventory().armor.get(3).copy());
		body.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem().copy());
		body.setItemSlot(EquipmentSlot.OFFHAND, player.getInventory().offhand.get(0).copy());
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

		float androidHealth = despawnAndroid(server, record);
		ControlSession session = capturePlayer(player, record);
		PlayerStandInEntity standIn = spawnStandIn(player, session);
		detachPlayer(player, session, standIn, record);

		data.startSession(session);
		((ServerLevel) player.level()).addFreshEntity(standIn);

		enterAndroidState(player, data, session, record, androidHealth);
		ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ControlStatePacket(true));

		return Result.SUCCESS;
	}

	private static float despawnAndroid(MinecraftServer server, AndroidRecord record) {
		ServerLevel androidLevel = server.getLevel(record.dimension);
		forceLoadChunk(androidLevel, record.pos());
		Entity found = androidLevel.getEntity(record.id);
		if (found instanceof AndroidEntity androidEntity) {
			float health = androidEntity.getHealth();
			androidEntity.discard();
			return health;
		}
		return record.health;
	}

	private static ControlSession capturePlayer(ServerPlayer player, AndroidRecord record) {
		ServerLevel originalLevel = (ServerLevel) player.level();
		Vec3 originalPos = player.position();
		float originalYaw = player.getYRot();
		float originalPitch = player.getXRot();
		GameType originalGameType = player.gameMode.getGameModeForPlayer();
		float originalHealth = player.getHealth();
		ListTag savedInventory = player.getInventory().save(new ListTag());

		return new ControlSession(player.getUUID(), record.id, UUID.randomUUID(), record.androidType,
				originalLevel.dimension(), originalPos, originalYaw, originalPitch, originalGameType, originalHealth,
				savedInventory);
	}

	private static PlayerStandInEntity spawnStandIn(ServerPlayer player, ControlSession session) {
		ServerLevel originalLevel = (ServerLevel) player.level();
		PlayerStandInEntity standIn = new PlayerStandInEntity(ModEntityTypes.PLAYER_STAND_IN.get(), originalLevel);
		standIn.setUUID(session.standInId);
		standIn.setOwnerUuid(player.getUUID());
		Vec3 pos = session.pos();
		standIn.moveTo(pos.x, pos.y, pos.z, session.yaw, session.pitch);
		standIn.setYHeadRot(session.yaw);
		standIn.setYBodyRot(session.yaw);
		standIn.setYRot(session.yaw);
		standIn.setXRot(session.pitch);
		standIn.setCustomName(Component.literal(player.getGameProfile().getName()));
		equipBody(player, standIn);
		if (Mods.COSMETIC_ARMOR.isLoaded()) {
			session.cosArmor = CosArmorHandler.snapshot(player);
			CosArmorHandler.apply(standIn, session.cosArmor);
		}
		return standIn;
	}

	private static void detachPlayer(ServerPlayer player, ControlSession session, PlayerStandInEntity standIn,
			AndroidRecord record) {
		if (Mods.CURIOS.isLoaded()) {
			session.curios = CuriosHandler.saveAndClear(player);
			if (record.androidType == AndroidType.SURVIVAL) {
				CuriosHandler.restore(player, record.curios);
			}
			CuriosHandler.restore(standIn, session.curios);
		}
		if (Mods.TFC.isLoaded()) {
			session.tfcFoodData = TFCHandler.saveFoodData(player);
			TFCHandler.resetFoodData(player);
		}
		session.effects = saveEffects(player);
		player.removeAllEffects();
		player.clearFire();
	}

	private static void enterAndroidState(ServerPlayer player, AndroidSavedData data, ControlSession session,
			AndroidRecord record, float androidHealth) {
		MinecraftServer server = player.getServer();
		ServerLevel androidLevel = server.getLevel(record.dimension);
		data.setAndroidIdle(session.androidId, false);
		data.updateAndroidState(session.androidId, androidLevel, record.pos(), record.yaw, record.pitch, androidHealth);

		player.teleportTo(androidLevel, record.x, record.y, record.z, record.yaw, record.pitch);

		if (session.androidType == AndroidType.SURVIVAL) {
			player.getInventory().clearContent();
			player.getInventory().load(record.inventory);
		} else {
			lockAllSlots(player);
		}

		GameType targetMode = session.androidType == AndroidType.SURVIVAL ? GameType.SURVIVAL : GameType.ADVENTURE;
		player.setGameMode(targetMode);
		player.setHealth(Math.max(1.0F, Math.min(player.getMaxHealth(), androidHealth)));
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(0.0F);
	}

	/** Prevent the inventory from being used and give a disconnect item. */
	public static void lockAllSlots(ServerPlayer player) {
		var inventory = player.getInventory();
		ItemStack lock = new ItemStack(ModItems.INVENTORY_LOCK.get());
		ItemStack disconnect = new ItemStack(ModItems.DISCONNECT.get());
		inventory.clearContent();
		inventory.setItem(0, disconnect);
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
		AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());
		ControlSession session = data.getSession(player.getUUID());
		boolean respawnAndroid = session == null || !session.dead;
		return swapOut(player, respawnAndroid);
	}

	public static Result swapOut(ServerPlayer player, boolean respawnAndroid) {
		AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());
		ControlSession session = data.getSession(player.getUUID());
		if (session == null) {
			return Result.NOT_CONTROLLING;
		}

		despawnStandIn(player.getServer(), session);
		respawnAndroid(player, data, session, respawnAndroid);
		restorePlayer(player, session);

		ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ControlStatePacket(false));

		data.setLastAndroid(player.getUUID(), session.androidId);

		data.endSession(player.getUUID());

		return Result.SUCCESS;
	}

	private static void despawnStandIn(MinecraftServer server, ControlSession session) {
		ServerLevel originalLevel = server.getLevel(session.originalDimension);
		forceLoadChunk(originalLevel, session.pos());
		Entity standIn = originalLevel.getEntity(session.standInId);
		if (standIn != null) {
			standIn.discard();
		}
	}

	private static void respawnAndroid(ServerPlayer player, AndroidSavedData data, ControlSession session,
			boolean respawnAndroid) {
		AndroidRecord savedAndroid = data.getAndroid(session.androidId);
		ServerLevel currentLevel = savedAndroid == null
				? (ServerLevel) player.level()
				: player.getServer().getLevel(savedAndroid.dimension);
		Vec3 currentPos = savedAndroid == null ? player.position() : savedAndroid.pos();
		float currentYaw = savedAndroid == null ? player.getYHeadRot() : savedAndroid.yaw;
		float currentPitch = savedAndroid == null ? player.getXRot() : savedAndroid.pitch;

		if (!respawnAndroid) {
			data.removeAndroid(session.androidId);
			return;
		}

		data.setAndroidIdle(session.androidId, true);

		AndroidEntity android = new AndroidEntity(ModEntityTypes.ANDROID.get(), currentLevel);
		android.setUUID(session.androidId);
		android.setAndroidType(session.androidType);
		android.moveTo(currentPos.x, currentPos.y, currentPos.z, currentYaw, currentPitch);
		android.setYHeadRot(currentYaw);
		android.setYBodyRot(currentYaw);
		android.setYRot(currentYaw);
		android.setXRot(currentPitch);
		if (savedAndroid != null) {
			android.setHealth(Math.max(1.0F, Math.min(android.getMaxHealth(), savedAndroid.health)));
		}
		if (session.androidType == AndroidType.SURVIVAL) {
			equipBody(player, android);
			if (Mods.CURIOS.isLoaded()) {
				CuriosHandler.restore(android, CuriosHandler.saveSnapshot(player));
			}
		}
		currentLevel.addFreshEntity(android);

		if (session.androidType == AndroidType.SURVIVAL) {
			ListTag inventoryCopy = player.getInventory().save(new ListTag());
			data.updateAndroidInventory(session.androidId, inventoryCopy);
			if (Mods.CURIOS.isLoaded()) {
				ListTag curiosCopy = CuriosHandler.saveSnapshot(player);
				data.updateAndroidCurios(session.androidId, curiosCopy);
			}
		}

		data.updateAndroidState(session.androidId, currentLevel, currentPos, currentYaw, currentPitch,
				android.getHealth());
	}

	private static void restorePlayer(ServerPlayer player, ControlSession session) {
		MinecraftServer server = player.getServer();
		ServerLevel originalLevel = server.getLevel(session.originalDimension);
		player.teleportTo(originalLevel, session.x, session.y, session.z, session.yaw, session.pitch);
		player.setHealth(Math.max(1.0F, Math.min(player.getMaxHealth(), session.originalHealth)));
		player.getInventory().clearContent();
		player.getInventory().load(session.inventory);
		player.setGameMode(session.originalGameType);

		if (Mods.CURIOS.isLoaded()) {
			CuriosHandler.clear(player);
			CuriosHandler.restore(player, session.curios);
		}
		if (Mods.TFC.isLoaded()) {
			TFCHandler.restoreFoodData(player, session.tfcFoodData);
		}
		player.removeAllEffects();
		player.clearFire();
		restoreEffects(player, session.effects);
	}

	private static ListTag saveEffects(ServerPlayer player) {
		ListTag list = new ListTag();
		for (MobEffectInstance effect : player.getActiveEffects()) {
			list.add(effect.save(new CompoundTag()));
		}
		return list;
	}

	private static void restoreEffects(ServerPlayer player, ListTag effects) {
		for (int i = 0; i < effects.size(); i++) {
			MobEffectInstance effect = MobEffectInstance.load(effects.getCompound(i));
			if (effect != null) {
				player.addEffect(effect);
			}
		}
	}
}
