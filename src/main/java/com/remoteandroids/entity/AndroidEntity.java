package com.remoteandroids.entity;

import com.remoteandroids.data.AndroidSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Standby android body, can be damaged but has no AI. */
public class AndroidEntity extends PathfinderMob {
	private double savedX;
	private double savedY;
	private double savedZ;
	private float savedYaw;
	private float savedPitch;

	public AndroidEntity(EntityType<? extends AndroidEntity> type, Level level) {
		super(type, level);
		this.setNoAi(true);
		this.setNoGravity(false);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.ARMOR, 0.0D).add(Attributes.FOLLOW_RANGE, 0.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
	}

	@Override
	protected void registerGoals() {
		return;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isNoGravity()) {
			this.setNoGravity(false);
		}
		if (!this.level().isClientSide && (this.position().x != this.savedX || this.position().y != this.savedY
				|| this.position().z != this.savedZ || this.getYRot() != this.savedYaw
				|| this.getXRot() != this.savedPitch)) {
			var serverLevel = (ServerLevel) this.level();
			AndroidSavedData.get(serverLevel).updateAndroidState(this.getUUID(), serverLevel, this.position(),
					this.getYHeadRot(), this.getXRot(), this.getHealth());
			this.savedX = this.position().x;
			this.savedY = this.position().y;
			this.savedZ = this.position().z;
			this.savedYaw = this.getYHeadRot();
			this.savedPitch = this.getXRot();
		}
	}

	@Override
	protected ResourceLocation getDefaultLootTable() {
		// drop the spawning item
		return new ResourceLocation("remoteandroids", "entities/android");
	}

	/**
	 * Clean up AndroidSavedData record. Called from ModEventHandlers.onLivingDeath.
	 */
	public void onAndroidDeath() {
		if (!this.level().isClientSide) {
			AndroidSavedData.get((ServerLevel) this.level()).removeAndroid(this.getUUID());
		}
	}
}
