package com.remoteandroids.event;

import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.data.AndroidTransfer;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.PlayerStandInEntity;
import com.remoteandroids.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModEventHandlers {

	/** Clean up saved data after an android dies. */
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof AndroidEntity android) {
			android.onAndroidDeath();
			return;
		}

		if (event.getEntity() instanceof ServerPlayer player
				&& AndroidSavedData.get((ServerLevel) player.level()).isControllingAndroid(player.getUUID())) {
			ItemStack core = new ItemStack(ModItems.ANDROID_CORE.get());
			player.spawnAtLocation(core);
			event.setCanceled(true);
			player.setHealth(player.getMaxHealth());
			AndroidTransfer.swapOut(player, false);
		}
	}

	@SubscribeEvent
	public void onStandInHurt(LivingHurtEvent event) {
		if (!(event.getEntity() instanceof PlayerStandInEntity standIn) || standIn.level().isClientSide
				|| !(standIn.level() instanceof ServerLevel level)) {
			return;
		}
		var session = AndroidSavedData.get(level).getSessionByStandIn(standIn.getUUID());
		if (session != null) {
			ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
			if (player != null) {
				AndroidTransfer.swapOut(player);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
				|| !(event.player instanceof ServerPlayer player)) {
			return;
		}
		ServerLevel level = (ServerLevel) player.level();
		var data = AndroidSavedData.get(level);
		var session = data.getSession(player.getUUID());
		if (session == null)
			return;

		AndroidTransfer.fillAndroidInventory(player);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
		if (player.getHealth() > 20.0F) {
			player.setHealth(20.0F);
		}
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(0.0F);

		if (player.containerMenu != null) {
			for (var slot : player.containerMenu.slots) {
				if (slot.container == player.getInventory()) {
					continue;
				}
				ItemStack stack = slot.getItem();
				if (stack.is(ModItems.INVENTORY_LOCK.get()) || stack.is(ModItems.RECALL_CORE.get())) {
					slot.setByPlayer(ItemStack.EMPTY);
				}
			}
			player.containerMenu.broadcastChanges();
		}

		data.updateAndroidState(session.androidId, level, player.position(), player.getYHeadRot(), player.getXRot(),
				player.getHealth());

		ServerLevel originalLevel = player.getServer().getLevel(session.originalDimension);
		if (originalLevel == null)
			return;
		var standIn = originalLevel.getEntity(session.standInId);
		if (standIn != null && standIn.distanceToSqr(session.x, session.y, session.z) > 0.01D) {
			AndroidTransfer.swapOut(player);
		}
	}

	@SubscribeEvent
	public void onLogout(PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			AndroidTransfer.swapOut(player);
		}
	}

	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			AndroidTransfer.swapOut(player);
		}
	}

	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			AndroidTransfer.swapOut(player);
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {
		if (event.getPlayer().level() instanceof ServerLevel level
				&& AndroidSavedData.get(level).isControllingAndroid(event.getPlayer().getUUID())) {
			if (event.getEntity().getItem().is(ModItems.INVENTORY_LOCK.get())
					|| event.getEntity().getItem().is(ModItems.RECALL_CORE.get())) {
				event.setCanceled(true);
				if (event.getPlayer() instanceof ServerPlayer player) {
					AndroidTransfer.fillAndroidInventory(player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.getEntity() instanceof ServerPlayer player
				&& AndroidSavedData.get((ServerLevel) player.level()).isControllingAndroid(player.getUUID())) {
			event.setCanceled(true);
		}
	}
}
