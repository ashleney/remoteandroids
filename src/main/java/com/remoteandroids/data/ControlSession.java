package com.remoteandroids.data;

import com.remoteandroids.entity.AndroidEntity.AndroidType;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Player data at the moment they start to control an android. */
public class ControlSession {

	public UUID playerId;
	public UUID androidId;
	public UUID standInId;
	public AndroidType androidType;

	public ResourceKey<Level> originalDimension;
	public double x, y, z;
	public float yaw, pitch;
	public GameType originalGameType;
	public float originalHealth;

	public ListTag inventory;
	@Nullable
	public ListTag curios;
	@Nullable
	public CompoundTag tfcFoodData;
	public ListTag effects;
	/**
	 * Whether the controlling player is dead, used to destroy the android on
	 * swap-out
	 */
	public boolean dead;

	public ControlSession(UUID playerId, UUID androidId, UUID standInId, AndroidType androidType,
			ResourceKey<Level> originalDimension, Vec3 pos, float yaw, float pitch, GameType originalGameType,
			float originalHealth, ListTag inventory) {
		this.playerId = playerId;
		this.androidId = androidId;
		this.standInId = standInId;
		this.androidType = androidType;
		this.originalDimension = originalDimension;
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.originalGameType = originalGameType;
		this.originalHealth = originalHealth;
		this.inventory = inventory;
	}

	public Vec3 pos() {
		return new Vec3(x, y, z);
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Player", playerId);
		tag.putUUID("Android", androidId);
		tag.putUUID("StandIn", standInId);
		tag.putString("AndroidType", androidType.name());
		tag.putString("Dimension", originalDimension.location().toString());
		tag.putDouble("X", x);
		tag.putDouble("Y", y);
		tag.putDouble("Z", z);
		tag.putFloat("Yaw", yaw);
		tag.putFloat("Pitch", pitch);
		tag.putString("GameType", originalGameType.getName());
		tag.putFloat("Health", originalHealth);
		tag.put("Inventory", inventory);
		if (curios != null) {
			tag.put("Curios", curios);
		}
		if (tfcFoodData != null) {
			tag.put("TFCFoodData", tfcFoodData);
		}
		tag.put("Effects", effects);
		tag.putBoolean("Dead", dead);
		return tag;
	}

	public static ControlSession load(CompoundTag tag) {
		UUID player = tag.getUUID("Player");
		UUID android = tag.getUUID("Android");
		UUID standIn = tag.getUUID("StandIn");
		AndroidType type = AndroidType.byName(tag.getString("AndroidType"));
		ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
				new ResourceLocation(tag.getString("Dimension")));
		Vec3 pos = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
		float yaw = tag.getFloat("Yaw");
		float pitch = tag.getFloat("Pitch");
		GameType gameType = GameType.byName(tag.getString("GameType"));
		float health = tag.getFloat("Health");
		ListTag inventory = tag.getList("Inventory", 10);
		ListTag curios = tag.contains("Curios") ? tag.getList("Curios", 10) : null;
		ControlSession session = new ControlSession(player, android, standIn, type, dim, pos, yaw, pitch, gameType,
				health, inventory);
		session.curios = curios;
		session.tfcFoodData = tag.contains("TFCFoodData") ? tag.getCompound("TFCFoodData") : null;
		session.effects = tag.contains("Effects") ? tag.getList("Effects", 10) : new ListTag();
		session.dead = tag.getBoolean("Dead");
		return session;
	}
}
