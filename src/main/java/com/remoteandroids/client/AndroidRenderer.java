package com.remoteandroids.client;

import com.remoteandroids.compat.CompatCuriosRender;
import com.remoteandroids.compat.Mods;
import com.remoteandroids.entity.AndroidEntity;
import com.remoteandroids.entity.AndroidEntity.AndroidType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class AndroidRenderer extends MobRenderer<AndroidEntity, PlayerModel<AndroidEntity>> {
	private static final Map<AndroidType, ResourceLocation> DEFAULT_TEXTURES = new EnumMap<>(AndroidType.class);

	static {
		DEFAULT_TEXTURES.put(AndroidType.OBSERVER,
				new ResourceLocation("remoteandroids", "textures/entity/android/android_observer.png"));
		DEFAULT_TEXTURES.put(AndroidType.ADVENTURE,
				new ResourceLocation("remoteandroids", "textures/entity/android/android_adventure.png"));
		DEFAULT_TEXTURES.put(AndroidType.SURVIVAL,
				new ResourceLocation("remoteandroids", "textures/entity/android/android_survival.png"));
	}

	public AndroidRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this,
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		if (Mods.CURIOS.isLoaded()) {
			CompatCuriosRender.addLayer(this);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(AndroidEntity entity) {
		return DEFAULT_TEXTURES.getOrDefault(entity.getAndroidType(), DEFAULT_TEXTURES.get(AndroidType.ADVENTURE));
	}
}
