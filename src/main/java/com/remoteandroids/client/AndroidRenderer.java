package com.remoteandroids.client;

import com.remoteandroids.entity.AndroidEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AndroidRenderer extends MobRenderer<AndroidEntity, PlayerModel<AndroidEntity>> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("remoteandroids",
			"textures/entity/android/android.png");

	public AndroidRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(AndroidEntity entity) {
		return TEXTURE;
	}
}
