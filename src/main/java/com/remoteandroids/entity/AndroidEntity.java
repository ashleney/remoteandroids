package com.remoteandroids.entity;

import com.remoteandroids.compat.CuriosHandler;
import com.remoteandroids.compat.Mods;
import com.remoteandroids.data.AndroidRecord;
import com.remoteandroids.data.AndroidSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Standby android body, can be damaged but has no AI. */
public class AndroidEntity extends PathfinderMob {

	public enum AndroidType {
		OBSERVER, ADVENTURE, SURVIVAL;

		public static AndroidType byName(String name) {
			return valueOf(name);
		}

		public String getTranslationKey() {
			return "entity.remoteandroids.android." + name().toLowerCase();
		}
	}

	private static final EntityDataAccessor<String> ANDROID_TYPE = SynchedEntityData.defineId(AndroidEntity.class,
			EntityDataSerializers.STRING);

	private double savedX;
	private double savedY;
	private double savedZ;
	private float savedYaw;
	private float savedPitch;

	public AndroidEntity(EntityType<? extends AndroidEntity> type, Level level) {
		super(type, level);
		this.setNoAi(true);
		this.setNoGravity(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.ARMOR, 0.0D).add(Attributes.FOLLOW_RANGE, 0.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ANDROID_TYPE, AndroidType.ADVENTURE.name());
	}

	public AndroidType getAndroidType() {
		return AndroidType.byName(this.entityData.get(ANDROID_TYPE));
	}

	public void setAndroidType(AndroidType type) {
		this.entityData.set(ANDROID_TYPE, type.name());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("AndroidType", getAndroidType().name());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("AndroidType")) {
			setAndroidType(AndroidType.byName(tag.getString("AndroidType")));
		}
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
	public Component getName() {
		return Component.translatable(getAndroidType().getTranslationKey());
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			return;
		}

		ServerLevel serverLevel = (ServerLevel) this.level();
		AndroidRecord record = AndroidSavedData.get(serverLevel).getAndroid(this.getUUID());
		if (record != null && !record.idle) {
			this.discard();
			return;
		}
		if (this.position().x != this.savedX || this.position().y != this.savedY || this.position().z != this.savedZ
				|| this.getYRot() != this.savedYaw || this.getXRot() != this.savedPitch) {
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
		AndroidType type = getAndroidType();
		return new ResourceLocation("remoteandroids", "entities/android_" + type.name().toLowerCase());
	}

	/** Drop items when an idle android dies. */
	public void onAndroidDeath() {
		if (this.level().isClientSide) {
			return;
		}
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			this.setItemSlot(slot, ItemStack.EMPTY);
		}
		if (Mods.CURIOS.isLoaded()) {
			CuriosHandler.clear(this);
		}
		ServerLevel serverLevel = (ServerLevel) this.level();
		AndroidRecord record = AndroidSavedData.get(serverLevel).getAndroid(this.getUUID());
		if (record != null) {
			for (int i = 0; i < record.inventory.size(); i++) {
				CompoundTag itemTag = record.inventory.getCompound(i);
				ItemStack stack = ItemStack.of(itemTag);
				if (!stack.isEmpty()) {
					this.spawnAtLocation(stack);
				}
			}
			if (record.curios != null) {
				for (int i = 0; i < record.curios.size(); i++) {
					CompoundTag curiosTag = record.curios.getCompound(i);
					ListTag items = curiosTag.getList("Items", 10);
					for (int j = 0; j < items.size(); j++) {
						ItemStack stack = ItemStack.of(items.getCompound(j));
						if (!stack.isEmpty()) {
							this.spawnAtLocation(stack);
						}
					}
				}
			}
		}
		AndroidSavedData.get(serverLevel).removeAndroid(this.getUUID());
	}
}
