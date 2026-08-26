package com.remoteandroids.item;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/**
 * Dummy item that effectively prevents the player from using their inventory.
 */
public class InventoryLockItem extends Item {

	public InventoryLockItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		return true;
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
			Player player, SlotAccess access) {
		return true;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.empty();
	}
}
