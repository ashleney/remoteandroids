package com.remoteandroids.item;

import com.remoteandroids.data.AndroidSavedData;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.init.ModEntityTypes;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** On use spawns a new android. */
public class AndroidCoreItem extends Item {

	public AndroidCoreItem(Properties properties) {
		super(properties);
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
		android.moveTo(spawnPos.x, spawnPos.y, spawnPos.z,
				context.getPlayer() != null ? context.getPlayer().getYRot() : 0F, 0F);

		if (!serverLevel.addFreshEntity(android)) {
			return InteractionResult.FAIL;
		}

		AndroidSavedData.get(serverLevel).createAndroid(android.getUUID(), serverLevel.dimension(), android.position(),
				android.getYHeadRot(), android.getXRot(), android.getHealth());

		if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
			serverPlayer.displayClientMessage(
					Component.literal("A new android has been activated.").withStyle(ChatFormatting.GRAY), true);
			if (!serverPlayer.getAbilities().instabuild) {
				context.getItemInHand().shrink(1);
			}
		}

		return InteractionResult.CONSUME;
	}
}
