package com.extremo.jefes.client.render;

import com.extremo.jefes.ExtremoJefes;
import com.extremo.jefes.entity.LepismaSulfurico;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Lepisma con su propia textura amarilla de azufre (mismo modelo que la lepisma normal). */
public class LepismaSulfuricoRenderer extends MobRenderer<LepismaSulfurico, LivingEntityRenderState, SilverfishModel> {
	private static final Identifier TEXTURA = ExtremoJefes.id("textures/entity/lepisma_sulfurico.png");

	public LepismaSulfuricoRenderer(final EntityRendererProvider.Context context) {
		super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
	}

	@Override
	protected float getFlipDegrees() {
		return 180.0F;
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return TEXTURA;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}
