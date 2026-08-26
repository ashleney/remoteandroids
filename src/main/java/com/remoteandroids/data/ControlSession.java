package com.remoteandroids.data;

import java.util.UUID;
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

	public ResourceKey<Level> originalDimension;
	public double x, y, z;
	public float yaw, pitch;
	public GameType originalGameType;
	public float originalHealth;
	public double originalMaxHealth;

	public ListTag inventory;

	public ControlSession(UUID playerId, UUID androidId, UUID standInId, ResourceKey<Level> originalDimension, Vec3 pos,
			float yaw, float pitch, GameType originalGameType, float originalHealth, double originalMaxHealth,
			ListTag inventory) {
		this.playerId = playerId;
		this.androidId = androidId;
		this.standInId = standInId;
		this.originalDimension = originalDimension;
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.originalGameType = originalGameType;
		this.originalHealth = originalHealth;
		this.originalMaxHealth = originalMaxHealth;
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
		tag.putString("Dimension", originalDimension.location().toString());
		tag.putDouble("X", x);
		tag.putDouble("Y", y);
		tag.putDouble("Z", z);
		tag.putFloat("Yaw", yaw);
		tag.putFloat("Pitch", pitch);
		tag.putString("GameType", originalGameType.getName());
		tag.putFloat("Health", originalHealth);
		tag.putDouble("MaxHealth", originalMaxHealth);
		tag.put("Inventory", inventory);
		return tag;
	}

	public static ControlSession load(CompoundTag tag) {
		UUID player = tag.getUUID("Player");
		UUID android = tag.getUUID("Android");
		UUID standIn = tag.getUUID("StandIn");
		ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
				new ResourceLocation(tag.getString("Dimension")));
		Vec3 pos = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
		float yaw = tag.getFloat("Yaw");
		float pitch = tag.getFloat("Pitch");
		GameType gameType = GameType.byName(tag.getString("GameType"), GameType.SURVIVAL);
		float health = tag.getFloat("Health");
		double maxHealth = tag.getDouble("MaxHealth");
		ListTag inventory = tag.getList("Inventory", 10);
		return new ControlSession(player, android, standIn, dim, pos, yaw, pitch, gameType, health, maxHealth,
				inventory);
	}
}
