package com.remoteandroids.data;

import com.remoteandroids.entity.AndroidEntity.AndroidType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

/** Location of all androids and all players that are controlling an android. */
public class AndroidSavedData extends SavedData {

	private static final String DATA_NAME = "remoteandroids_data";

	private final Map<UUID, AndroidRecord> androids = new HashMap<>();
	private final Map<UUID, ControlSession> sessions = new HashMap<>(); // keyed by player UUID
	private final Map<UUID, UUID> lastAndroid = new HashMap<>(); // keyed by player UUID

	public static AndroidSavedData get(ServerLevel anyLevel) {
		ServerLevel overworld = anyLevel.getServer().getLevel(Level.OVERWORLD);
		return overworld.getDataStorage().computeIfAbsent(AndroidSavedData::load, AndroidSavedData::new, DATA_NAME);
	}

	public AndroidRecord createAndroid(UUID id, ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch,
			float health, AndroidType androidType) {
		AndroidRecord record = new AndroidRecord(id, dimension, pos, yaw, pitch, health, true, androidType,
				new ListTag(), null);
		androids.put(id, record);
		setDirty();
		return record;
	}

	@Nullable
	public AndroidRecord getAndroid(UUID id) {
		return androids.get(id);
	}

	public void updateAndroidState(UUID id, Level level, Vec3 pos, float yaw, float pitch, float health) {
		AndroidRecord record = androids.get(id);
		if (record != null) {
			record.dimension = level.dimension();
			record.x = pos.x;
			record.y = pos.y;
			record.z = pos.z;
			record.yaw = yaw;
			record.pitch = pitch;
			record.health = health;
			setDirty();
		}
	}

	public void setAndroidIdle(UUID id, boolean idle) {
		AndroidRecord record = androids.get(id);
		if (record != null) {
			record.idle = idle;
			setDirty();
		}
	}

	public void updateAndroidInventory(UUID id, ListTag inventory) {
		AndroidRecord record = androids.get(id);
		if (record != null) {
			record.inventory = inventory;
			setDirty();
		}
	}

	public void updateAndroidCurios(UUID id, ListTag curios) {
		AndroidRecord record = androids.get(id);
		if (record != null) {
			record.curios = curios;
			setDirty();
		}
	}

	public void removeAndroid(UUID id) {
		if (androids.remove(id) != null) {
			setDirty();
		}
		if (lastAndroid.values().remove(id)) {
			setDirty();
		}
	}

	public boolean isControllingAndroid(UUID playerId) {
		return sessions.containsKey(playerId);
	}

	@Nullable
	public ControlSession getSession(UUID playerId) {
		return sessions.get(playerId);
	}

	@Nullable
	public ControlSession getSessionByStandIn(UUID standInId) {
		for (ControlSession session : sessions.values()) {
			if (session.standInId.equals(standInId)) {
				return session;
			}
		}
		return null;
	}

	public void startSession(ControlSession session) {
		sessions.put(session.playerId, session);
		setDirty();
	}

	public void endSession(UUID playerId) {
		if (sessions.remove(playerId) != null) {
			setDirty();
		}
	}

	public void updateSessionDead(UUID playerId, boolean dead) {
		ControlSession session = sessions.get(playerId);
		if (session != null) {
			session.dead = dead;
			setDirty();
		}
	}

	public void setLastAndroid(UUID playerId, UUID androidId) {
		lastAndroid.put(playerId, androidId);
		setDirty();
	}

	@Nullable
	public UUID getLastAndroid(UUID playerId) {
		return lastAndroid.get(playerId);
	}

	public static AndroidSavedData load(CompoundTag tag) {
		AndroidSavedData data = new AndroidSavedData();
		ListTag androidList = tag.getList("Androids", 10);
		for (int i = 0; i < androidList.size(); i++) {
			AndroidRecord record = AndroidRecord.load(androidList.getCompound(i));
			data.androids.put(record.id, record);
		}
		ListTag sessionList = tag.getList("Sessions", 10);
		for (int i = 0; i < sessionList.size(); i++) {
			ControlSession session = ControlSession.load(sessionList.getCompound(i));
			data.sessions.put(session.playerId, session);
		}
		ListTag lastList = tag.getList("LastAndroid", 10);
		for (int i = 0; i < lastList.size(); i++) {
			CompoundTag entry = lastList.getCompound(i);
			data.lastAndroid.put(entry.getUUID("Player"), entry.getUUID("Android"));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag androidList = new ListTag();
		for (AndroidRecord record : androids.values()) {
			androidList.add(record.save());
		}
		tag.put("Androids", androidList);

		ListTag sessionList = new ListTag();
		for (ControlSession session : sessions.values()) {
			sessionList.add(session.save());
		}
		tag.put("Sessions", sessionList);

		ListTag lastList = new ListTag();
		for (Map.Entry<UUID, UUID> entry : lastAndroid.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putUUID("Player", entry.getKey());
			entryTag.putUUID("Android", entry.getValue());
			lastList.add(entryTag);
		}
		tag.put("LastAndroid", lastList);
		return tag;
	}
}
