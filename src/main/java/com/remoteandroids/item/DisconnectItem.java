package com.remoteandroids.item;

import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.data.AndroidTransfer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

/** Use to return to your body while controlling an android. */
public class DisconnectItem extends Item {

	public DisconnectItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		tooltip.add(Component.translatable("item.remoteandroids.disconnect.desc").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(stack);
		}

		if (!AndroidSavedData.get((net.minecraft.server.level.ServerLevel) level)
				.isControllingAndroid(player.getUUID())) {
			serverPlayer.displayClientMessage(
					Component.translatable("remoteandroids.msg.not_controlling").withStyle(ChatFormatting.RED), true);
			return InteractionResultHolder.fail(stack);
		}

		AndroidTransfer.Result result = AndroidTransfer.swapOut(serverPlayer);
		if (result == AndroidTransfer.Result.SUCCESS) {
			serverPlayer.displayClientMessage(
					Component.translatable("remoteandroids.msg.welcome_back").withStyle(ChatFormatting.AQUA), true);
		}

		return InteractionResultHolder.success(stack);
	}
}
