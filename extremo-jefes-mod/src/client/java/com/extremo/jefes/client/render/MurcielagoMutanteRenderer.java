package com.extremo.jefes.client.render;

import com.extremo.jefes.ExtremoJefes;
import com.extremo.jefes.client.model.MurcielagoMutanteModel;
import com.extremo.jefes.entity.MurcielagoMutante;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

public class MurcielagoMutanteRenderer extends MobRenderer<MurcielagoMutante, MurcielagoMutanteRenderState, MurcielagoMutanteModel> {
	public static final float ESCALA = 2.2F;
	private static final Identifier TEXTURA = ExtremoJefes.id("textures/entity/murcielago_mutante/murcielago_mutante.png");
	private static final RenderType OJOS = RenderTypes.eyes(ExtremoJefes.id("textures/entity/murcielago_mutante/murcielago_mutante_ojos.png"));
	private static final RenderType OJOS_FURIA = RenderTypes.eyes(ExtremoJefes.id("textures/entity/murcielago_mutante/murcielago_mutante_ojos_furia.png"));

	public MurcielagoMutanteRenderer(final EntityRendererProvider.Context context) {
		super(context, new MurcielagoMutanteModel(context.bakeLayer(MurcielagoMutanteModel.LAYER)), 1.4F);
		this.addLayer(new OjosLayer(this));
	}

	@Override
	public Identifier getTextureLocation(final MurcielagoMutanteRenderState state) {
		return TEXTURA;
	}

	@Override
	public MurcielagoMutanteRenderState createRenderState() {
		return new MurcielagoMutanteRenderState();
	}

	@Override
	public void extractRenderState(final MurcielagoMutante entity, final MurcielagoMutanteRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.flyAnimationState.copyFrom(entity.flyAnimationState);
		state.enfurecido = entity.isEnfurecido();
	}

	@Override
	protected void scale(final MurcielagoMutanteRenderState state, final PoseStack poseStack) {
		poseStack.scale(ESCALA, ESCALA, ESCALA);
	}

	@Override
	protected AABB getBoundingBoxForCulling(final MurcielagoMutante entity) {
		// Las alas sobresalen mucho de la caja de colisión.
		return super.getBoundingBoxForCulling(entity).inflate(3.0, 1.5, 3.0);
	}

	/** Ojos amarillos que brillan en la oscuridad (rojos cuando está enfurecido). */
	private static class OjosLayer extends RenderLayer<MurcielagoMutanteRenderState, MurcielagoMutanteModel> {
		OjosLayer(final RenderLayerParent<MurcielagoMutanteRenderState, MurcielagoMutanteModel> parent) {
			super(parent);
		}

		@Override
		public void submit(
			final PoseStack poseStack, final SubmitNodeCollector collector, final int lightCoords, final MurcielagoMutanteRenderState state,
			final float yRot, final float xRot
		) {
			RenderType tipo = state.enfurecido ? OJOS_FURIA : OJOS;
			collector.order(1).submitModel(this.getParentModel(), state, poseStack, tipo, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		}
	}
}
