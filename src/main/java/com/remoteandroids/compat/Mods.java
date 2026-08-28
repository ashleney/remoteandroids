package com.remoteandroids.compat;

import net.minecraftforge.fml.ModList;

/** Runtime mod presence checks. */
public enum Mods {
	CURIOS("curios"), TFC("tfc");

	private final String modId;
	private Boolean loaded;

	Mods(String modId) {
		this.modId = modId;
	}

	public boolean isLoaded() {
		if (loaded == null) {
			loaded = ModList.get().isLoaded(modId);
		}
		return loaded;
	}
}
