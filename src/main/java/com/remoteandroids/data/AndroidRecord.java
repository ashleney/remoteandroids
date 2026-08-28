package com.remoteandroids.data;

import com.remoteandroids.entity.AndroidEntity.AndroidType;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Persisted state for an android */
public class AndroidRecord {

	public UUID id;
	public ResourceKey<Level> dimension;
	public double x, y, z;
	public float yaw, pitch;
	public float health;
	public boolean idle;
	public AndroidType androidType;
	public ListTag inventory;
	@Nullable
	public ListTag curios;

	public AndroidRecord(UUID id, ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch, float health,
			boolean idle, AndroidType androidType, ListTag inventory, @Nullable ListTag curios) {
		this.id = id;
		this.dimension = dimension;
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.health = health;
		this.idle = idle;
		this.androidType = androidType;
		this.inventory = inventory;
		this.curios = curios;
	}

	public Vec3 pos() {
		return new Vec3(x, y, z);
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("Id", id);
		tag.putString("Dimension", dimension.location().toString());
		tag.putDouble("X", x);
		tag.putDouble("Y", y);
		tag.putDouble("Z", z);
		tag.putFloat("Yaw", yaw);
		tag.putFloat("Pitch", pitch);
		tag.putFloat("Health", health);
		tag.putBoolean("Idle", idle);
		tag.putString("AndroidType", androidType.name());
		tag.put("Inventory", inventory);
		if (curios != null) {
			tag.put("Curios", curios);
		}
		return tag;
	}

	public static AndroidRecord load(CompoundTag tag) {
		UUID id = tag.getUUID("Id");
		ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
				new ResourceLocation(tag.getString("Dimension")));
		AndroidType type = AndroidType.byName(tag.getString("AndroidType"));
		ListTag inventory = tag.contains("Inventory") ? tag.getList("Inventory", 10) : new ListTag();
		ListTag curios = tag.contains("Curios") ? tag.getList("Curios", 10) : null;
		return new AndroidRecord(id, dim, new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")),
				tag.getFloat("Yaw"), tag.getFloat("Pitch"), tag.getFloat("Health"), tag.getBoolean("Idle"), type,
				inventory, curios);
	}
}
