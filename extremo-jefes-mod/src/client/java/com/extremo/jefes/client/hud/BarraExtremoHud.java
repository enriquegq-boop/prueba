package com.extremo.jefes.client.hud;

import com.extremo.jefes.ExtremoJefes;
import java.util.Collection;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * Barra de vida estilo Extremo, arriba a la izquierda: medallón dorado con goterones de azufre,
 * barra dorada con relleno naranja-amarillo y el nombre del jefe centrado debajo.
 * En furia (barra marcada en rojo por el servidor) el relleno y el medallón se ponen rojos.
 */
public class BarraExtremoHud implements HudElement {
	public static final Identifier ID = ExtremoJefes.id("barra_extremo");
	private static final Identifier TEXTURA = ExtremoJefes.id("textures/gui/barra_extremo.png");
	private static final int TEX_ANCHO = 256;
	private static final int TEX_ALTO = 80;

	/** Tamaño del marco (medallón + barra + adornos). */
	public static final int ANCHO = 160;
	public static final int ALTO = 32;
	/** Relleno dentro del marco. */
	private static final int RELLENO_X = 26;
	private static final int RELLENO_Y = 9;
	private static final int RELLENO_ANCHO = 120;
	private static final int RELLENO_ALTO = 7;
	/** Centro horizontal de la barra (para el nombre). */
	private static final int CENTRO_BARRA = 86;
	private static final int MARGEN = 4;
	private static final int SEPARACION = 34;

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker) {
		Collection<LerpingBossEvent> barras = BarrasExtremoCliente.barras();
		if (barras.isEmpty()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		int x = MARGEN;
		int y = MARGEN;
		for (LerpingBossEvent barra : barras) {
			boolean furia = barra.getColor() == BossEvent.BossBarColor.RED;
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURA, x, y, 0.0F, furia ? 32.0F : 0.0F, ANCHO, ALTO, TEX_ANCHO, TEX_ALTO);
			int relleno = Mth.lerpDiscrete(Mth.clamp(barra.getProgress(), 0.0F, 1.0F), 0, RELLENO_ANCHO);
			if (relleno > 0) {
				graphics.blit(
					RenderPipelines.GUI_TEXTURED, TEXTURA, x + RELLENO_X, y + RELLENO_Y, 0.0F, furia ? 72.0F : 64.0F, relleno, RELLENO_ALTO, TEX_ANCHO, TEX_ALTO
				);
			}
			graphics.centeredText(minecraft.font, barra.getName(), x + CENTRO_BARRA, y + 20, furia ? 0xFFFFB0A0 : 0xFFF4E8CC);
			y += SEPARACION;
			if (y > graphics.guiHeight() / 3) {
				break;
			}
		}
	}
}
