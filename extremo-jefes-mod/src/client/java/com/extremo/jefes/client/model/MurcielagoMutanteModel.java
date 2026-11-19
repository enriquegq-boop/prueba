package com.extremo.jefes.client.model;

import com.extremo.jefes.ExtremoJefes;
import com.extremo.jefes.client.render.MurcielagoMutanteRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.BatAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Modelo del Murciélago Mutante: un murciélago enorme con cuernos, colmillos y
 * cristales de azufre creciéndole en la espalda. Usa los mismos nombres de piezas que el
 * murciélago normal para reutilizar su animación de aleteo.
 */
public class MurcielagoMutanteModel extends EntityModel<MurcielagoMutanteRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(ExtremoJefes.id("murcielago_mutante"), "main");

	private final ModelPart head;
	private final KeyframeAnimation flyingAnimation;

	public MurcielagoMutanteModel(final ModelPart root) {
		super(root, RenderTypes::entityCutoutCull);
		this.head = root.getChild("head");
		this.flyingAnimation = BatAnimation.BAT_FLYING.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 10.0F, 4.0F), PartPose.offset(0.0F, 9.0F, 0.0F)
		);
		// Cristales de azufre en la espalda.
		body.addOrReplaceChild(
			"cristal_1", CubeListBuilder.create().texOffs(32, 30).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, 2.0F, 2.0F, -0.6F, 0.0F, 0.3F)
		);
		body.addOrReplaceChild(
			"cristal_2", CubeListBuilder.create().texOffs(40, 30).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
			PartPose.offsetAndRotation(-1.5F, 5.0F, 2.0F, -0.9F, 0.0F, -0.4F)
		);
		body.addOrReplaceChild(
			"cristal_3", CubeListBuilder.create().texOffs(48, 30).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
			PartPose.offsetAndRotation(1.5F, 7.0F, 2.0F, -1.0F, 0.0F, 0.5F)
		);

		PartDefinition head = root.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 9.0F, 0.0F)
		);
		head.addOrReplaceChild("hocico", CubeListBuilder.create().texOffs(28, 16).addBox(-2.0F, -3.0F, -5.0F, 4.0F, 3.0F, 2.0F), PartPose.ZERO);
		head.addOrReplaceChild("colmillo_derecho", CubeListBuilder.create().texOffs(40, 16).addBox(-1.5F, 0.0F, -5.0F, 1.0F, 2.0F, 1.0F), PartPose.ZERO);
		head.addOrReplaceChild("colmillo_izquierdo", CubeListBuilder.create().texOffs(44, 16).addBox(0.5F, 0.0F, -5.0F, 1.0F, 2.0F, 1.0F), PartPose.ZERO);
		head.addOrReplaceChild(
			"right_ear", CubeListBuilder.create().texOffs(0, 30).addBox(-4.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F),
			PartPose.offsetAndRotation(-2.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.3F)
		);
		head.addOrReplaceChild(
			"left_ear", CubeListBuilder.create().texOffs(8, 30).addBox(0.0F, -8.0F, 0.0F, 4.0F, 8.0F, 0.0F),
			PartPose.offsetAndRotation(2.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.3F)
		);
		head.addOrReplaceChild(
			"cuerno_derecho", CubeListBuilder.create().texOffs(16, 30).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
			PartPose.offsetAndRotation(-3.0F, -5.0F, -1.0F, -0.4F, 0.0F, -0.4F)
		);
		head.addOrReplaceChild(
			"cuerno_izquierdo", CubeListBuilder.create().texOffs(24, 30).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F),
			PartPose.offsetAndRotation(3.0F, -5.0F, -1.0F, -0.4F, 0.0F, 0.4F)
		);

		PartDefinition rightWing = body.addOrReplaceChild(
			"right_wing", CubeListBuilder.create().texOffs(48, 0).addBox(-4.0F, -2.0F, 0.0F, 4.0F, 14.0F, 0.0F), PartPose.offset(-3.0F, 1.0F, 0.0F)
		);
		rightWing.addOrReplaceChild(
			"right_wing_tip", CubeListBuilder.create().texOffs(64, 0).addBox(-12.0F, -2.0F, 0.0F, 12.0F, 16.0F, 0.0F), PartPose.offset(-4.0F, 0.0F, 0.0F)
		);
		PartDefinition leftWing = body.addOrReplaceChild(
			"left_wing", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -2.0F, 0.0F, 4.0F, 14.0F, 0.0F), PartPose.offset(3.0F, 1.0F, 0.0F)
		);
		leftWing.addOrReplaceChild(
			"left_wing_tip", CubeListBuilder.create().texOffs(88, 0).addBox(0.0F, -2.0F, 0.0F, 12.0F, 16.0F, 0.0F), PartPose.offset(4.0F, 0.0F, 0.0F)
		);
		body.addOrReplaceChild("feet", CubeListBuilder.create().texOffs(48, 16).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 3.0F, 0.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(final MurcielagoMutanteRenderState state) {
		super.setupAnim(state);
		// Aleteo más lento que un murciélago normal: es enorme.
		this.flyingAnimation.apply(state.flyAnimationState, state.ageInTicks, state.enfurecido ? 0.8F : 0.55F);
		this.head.yRot += state.yRot * (float) (Math.PI / 180.0) * 0.6F;
		this.head.xRot += state.xRot * (float) (Math.PI / 180.0) * 0.5F;
	}
}
