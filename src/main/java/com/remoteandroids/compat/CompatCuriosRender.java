package com.remoteandroids.compat;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.client.render.CuriosLayer;

@OnlyIn(Dist.CLIENT)
public final class CompatCuriosRender {

	private CompatCuriosRender() {
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void addLayer(
			LivingEntityRenderer<T, M> renderer) {
		renderer.addLayer(new CuriosLayer<>(renderer));
	}
}
