package com.remoteandroids.data;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
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

	public AndroidRecord(UUID id, ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch, float health,
			boolean idle) {
		this.id = id;
		this.dimension = dimension;
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.health = health;
		this.idle = idle;
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
		return tag;
	}

	public static AndroidRecord load(CompoundTag tag) {
		UUID id = tag.getUUID("Id");
		ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
				new ResourceLocation(tag.getString("Dimension")));
		AndroidRecord record = new AndroidRecord(id, dim,
				new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")), tag.getFloat("Yaw"),
				tag.getFloat("Pitch"), tag.getFloat("Health"), tag.getBoolean("Idle"));
		return record;
	}
}
