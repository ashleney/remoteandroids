package com.remoteandroids.compat;

import net.minecraftforge.common.MinecraftForge;

/** Registers compat event handlers for optional mods. */
public final class ModCompat {

	private ModCompat() {
	}

	public static void register() {
		if (Mods.TFC.isLoaded()) {
			MinecraftForge.EVENT_BUS.register(new TFCHandler());
		}
	}
}
