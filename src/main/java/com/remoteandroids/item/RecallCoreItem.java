package com.remoteandroids.item;

import com.remoteandroids.data.AndroidTransfer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecallCoreItem extends Item {

	public RecallCoreItem(Properties properties) {
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
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(stack);
		}

		AndroidTransfer.Result result = AndroidTransfer.swapOut(serverPlayer);
		switch (result) {
			case SUCCESS -> {
				serverPlayer.displayClientMessage(Component.literal("Welcome back.").withStyle(ChatFormatting.AQUA),
						true);
				return InteractionResultHolder.success(stack);
			}
			case NOT_CONTROLLING -> serverPlayer.displayClientMessage(
					Component.literal("You aren't inhabiting an android right now.").withStyle(ChatFormatting.RED),
					true);
			default -> {
			}
		}

		return InteractionResultHolder.fail(stack);
	}
}
