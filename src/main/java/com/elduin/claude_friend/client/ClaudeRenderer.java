package com.elduin.claude_friend.client;

import com.elduin.claude_friend.entity.ClaudeEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Claude looks like Steve: the player model, with the game's own Steve skin. */
public class ClaudeRenderer extends HumanoidMobRenderer<ClaudeEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	private static final Identifier STEVE = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");

	public ClaudeRenderer(EntityRendererProvider.Context context) {
		// the player layer, so the jacket, sleeves and trousers layers show too
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return STEVE;
	}
}
