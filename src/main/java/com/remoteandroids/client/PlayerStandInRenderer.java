package com.remoteandroids.client;

import com.remoteandroids.entity.PlayerStandInEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PlayerStandInRenderer extends MobRenderer<PlayerStandInEntity, PlayerModel<PlayerStandInEntity>> {
	public PlayerStandInRenderer(EntityRendererProvider.Context context) {
		super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(PlayerStandInEntity entity) {
		if (entity.getOwnerUuid() != null) {
			PlayerInfo info = Minecraft.getInstance().getConnection() == null
					? null
					: Minecraft.getInstance().getConnection().getPlayerInfo(entity.getOwnerUuid());
			if (info != null) {
				return info.getSkinLocation();
			}
			return DefaultPlayerSkin.getDefaultSkin(entity.getOwnerUuid());
		}
		return DefaultPlayerSkin.getDefaultSkin();
	}
}
