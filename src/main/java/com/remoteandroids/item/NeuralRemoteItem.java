package com.remoteandroids.item;

import com.remoteandroids.data.AndroidTransfer;
import com.remoteandroids.entity.AndroidEntity;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Binds itself to an android and swaps the player on use. */
public class NeuralRemoteItem extends Item {

	private static final String TAG_UUID = "UUID";

	public NeuralRemoteItem(Properties properties) {
		super(properties);
	}

	@Nullable
	public static UUID getBoundAndroid(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.hasUUID(TAG_UUID)) {
			return tag.getUUID(TAG_UUID);
		}
		return null;
	}

	private static void setBoundAndroid(ItemStack stack, UUID id) {
		stack.getOrCreateTag().putUUID(TAG_UUID, id);
	}

	private static void clearBoundAndroid(ItemStack stack) {
		if (stack.hasTag()) {
			stack.getTag().remove(TAG_UUID);
		}
	}

	/** Binds the remote control to an android when right-clicked. */
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
			InteractionHand hand) {
		if (!(target instanceof AndroidEntity android)) {
			return InteractionResult.PASS;
		}

		if (player.level().isClientSide) {
			return InteractionResult.SUCCESS;
		}

		ItemStack serverStack = player.getItemInHand(hand);
		UUID currentlyBound = getBoundAndroid(serverStack);
		if (currentlyBound != null && currentlyBound.equals(android.getUUID())) {
			player.displayClientMessage(
					Component.literal("This remote is already bound to that android.").withStyle(ChatFormatting.YELLOW),
					true);
			return InteractionResult.SUCCESS;
		}

		setBoundAndroid(serverStack, android.getUUID());
		player.setItemInHand(hand, serverStack);
		player.getInventory().setChanged();

		player.displayClientMessage(Component
				.literal(currentlyBound == null ? "Remote bound to android." : "Remote re-bound to a new android.")
				.withStyle(ChatFormatting.AQUA), true);

		return InteractionResult.SUCCESS;
	}

	/** Triggers the swap. */
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(stack);
		}

		UUID boundId = getBoundAndroid(stack);
		if (boundId == null) {
			serverPlayer.displayClientMessage(
					Component.literal("This remote isn't bound yet. Right-click an android to bind it.")
							.withStyle(ChatFormatting.RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		AndroidTransfer.Result result = AndroidTransfer.swapIn(serverPlayer, boundId);
		switch (result) {
			case SUCCESS -> serverPlayer.displayClientMessage(
					Component.literal("Consciousness transferred.").withStyle(ChatFormatting.AQUA), true);
			case ALREADY_CONTROLLING -> serverPlayer.displayClientMessage(
					Component.literal("You are already inhabiting an android.").withStyle(ChatFormatting.RED), true);
			case ANDROID_BUSY -> serverPlayer.displayClientMessage(
					Component.literal("That android is currently occupied.").withStyle(ChatFormatting.RED), true);
			case ANDROID_MISSING -> {
				clearBoundAndroid(stack);
				serverPlayer.displayClientMessage(Component.literal("That android no longer exists. Remote unbound.")
						.withStyle(ChatFormatting.RED), true);
			}
			default -> {
			}
		}

		return result == AndroidTransfer.Result.SUCCESS
				? InteractionResultHolder.success(stack)
				: InteractionResultHolder.fail(stack);
	}
}
