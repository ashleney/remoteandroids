package com.remoteandroids.event;

import com.remoteandroids.compat.Mods;
import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.data.AndroidTransfer;
import com.remoteandroids.data.ControlSession;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.AndroidEntity.AndroidType;
import com.remoteandroids.entity.PlayerStandInEntity;
import com.remoteandroids.init.ModItems;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
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
			AndroidSavedData data = AndroidSavedData.get((ServerLevel) player.level());
			ControlSession session = data.getSession(player.getUUID());
			if (session.androidType == AndroidType.SURVIVAL) {
				player.spawnAtLocation(new ItemStack(getCoreForType(session.androidType)));
				data.updateSessionDead(player.getUUID(), true);
			} else {
				player.spawnAtLocation(new ItemStack(getCoreForType(session.androidType)));
				event.setCanceled(true);
				player.setHealth(player.getMaxHealth());
				AndroidTransfer.swapOut(player, false);
			}
		}
	}

	private static Item getCoreForType(AndroidType type) {
		return switch (type) {
			case OBSERVER -> ModItems.OBSERVER_CORE.get();
			case SURVIVAL -> ModItems.SURVIVAL_CORE.get();
			case ADVENTURE -> ModItems.ADVENTURE_CORE.get();
		};
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
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
			ControlSession session = AndroidSavedData.get((ServerLevel) player.level()).getSession(player.getUUID());
			if (session != null && session.androidType == AndroidType.OBSERVER) {
				event.setCanceled(true);
				event.setUseBlock(Event.Result.DENY);
				event.setUseItem(Event.Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
			ControlSession session = AndroidSavedData.get((ServerLevel) player.level()).getSession(player.getUUID());
			if (session != null && session.androidType == AndroidType.OBSERVER) {
				event.setCanceled(true);
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
		if (session == null || player.isDeadOrDying()) {
			return;
		}

		if (Mods.COSMETIC_ARMOR.isLoaded() && player.containerMenu instanceof ContainerCosArmor) {
			player.closeContainer();
		}

		if (session.androidType != AndroidType.SURVIVAL) {
			AndroidTransfer.lockAllSlots(player);
		}

		if (!Mods.TFC.isLoaded()) {
			player.getFoodData().setFoodLevel(20);
			player.getFoodData().setSaturation(0.0F);
		}

		if (session.androidType != AndroidType.SURVIVAL) {
			for (var slot : player.containerMenu.slots) {
				if (slot.container == player.getInventory()) {
					continue;
				}
				ItemStack stack = slot.getItem();
				if (stack.is(ModItems.INVENTORY_LOCK.get()) || stack.is(ModItems.DISCONNECT.get())) {
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
		if (event.getPlayer().level() instanceof ServerLevel level) {
			ControlSession session = AndroidSavedData.get(level).getSession(event.getPlayer().getUUID());
			if (session != null) {
				ItemStack tossed = event.getEntity().getItem();
				if (tossed.is(ModItems.DISCONNECT.get()) || tossed.is(ModItems.INVENTORY_LOCK.get())) {
					event.setCanceled(true);
					if (session.androidType != AndroidType.SURVIVAL
							&& event.getPlayer() instanceof ServerPlayer player) {
						AndroidTransfer.lockAllSlots(player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
			ControlSession session = AndroidSavedData.get(level).getSession(player.getUUID());
			if (session != null && session.androidType != AndroidType.SURVIVAL) {
				event.setCanceled(true);
			}
		}
	}
}
