package com.remoteandroids.item;

import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.AndroidEntity.AndroidType;
import com.remoteandroids.init.ModEntityTypes;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** On use spawns a new android of a given type. */
public class AndroidCoreItem extends Item {

	private final AndroidType androidType;

	public AndroidCoreItem(Properties properties, AndroidType androidType) {
		super(properties);
		this.androidType = androidType;
	}

	public AndroidType getAndroidType() {
		return androidType;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		tooltip.add(Component.translatable("item.remoteandroids." + androidType.name().toLowerCase() + "_core.desc")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		BlockPos placeAt = context.getClickedPos().relative(context.getClickedFace());
		Vec3 spawnPos = Vec3.atBottomCenterOf(placeAt);

		AndroidEntity android = new AndroidEntity(ModEntityTypes.ANDROID.get(), serverLevel);
		android.setUUID(UUID.randomUUID());
		android.setAndroidType(androidType);
		android.moveTo(spawnPos.x, spawnPos.y, spawnPos.z,
				context.getPlayer() != null ? context.getPlayer().getYRot() : 0F, 0F);

		if (!serverLevel.addFreshEntity(android)) {
			return InteractionResult.FAIL;
		}

		AndroidSavedData.get(serverLevel).createAndroid(android.getUUID(), serverLevel.dimension(), android.position(),
				android.getYHeadRot(), android.getXRot(), android.getHealth(), androidType);

		if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
			serverPlayer.displayClientMessage(
					Component.translatable("remoteandroids.msg.android_activated").withStyle(ChatFormatting.GRAY),
					true);
			if (!serverPlayer.getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
		}

		return InteractionResult.CONSUME;
	}
}
