package com.remoteandroids.entity;

import com.remoteandroids.data.AndroidSavedData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Standby player body, has no AI, damaging it makes the player go back to their
 * body, uses the player's skin.
 */
public class PlayerStandInEntity extends PathfinderMob {
	private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData
			.defineId(PlayerStandInEntity.class, EntityDataSerializers.OPTIONAL_UUID);

	public PlayerStandInEntity(EntityType<? extends PlayerStandInEntity> type, Level level) {
		super(type, level);
		this.setNoAi(true);
		this.setNoGravity(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.FOLLOW_RANGE, 0.0D);
	}

	@Override
	protected void registerGoals() {
		return;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(OWNER_UUID, Optional.empty());
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
			if (AndroidSavedData.get(serverLevel).getSessionByStandIn(this.getUUID()) == null) {
				this.discard();
			}
		}
	}

	public void setOwnerUuid(UUID ownerUuid) {
		this.entityData.set(OWNER_UUID, Optional.of(ownerUuid));
	}

	public UUID getOwnerUuid() {
		return this.entityData.get(OWNER_UUID).orElse(null);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		UUID owner = getOwnerUuid();
		if (owner != null) {
			tag.putUUID("Owner", owner);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("Owner")) {
			setOwnerUuid(tag.getUUID("Owner"));
		}
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) {
		return false;
	}
}
