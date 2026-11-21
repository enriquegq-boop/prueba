package com.extremo.jefes.client.hud;

import com.extremo.jefes.ExtremoJefes;
import com.extremo.jefes.IntegracionExtremo3;
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
		if (IntegracionExtremo3.cargado()) {
			this.dibujarEstiloExtremo3(graphics, minecraft, barras);
			return;
		}
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

	// ---------------------------------------------------------------------------------------------
	// Misma barra que usan el Dragón de Azufre y el Piglin Brutal Mutante del mod Extremo3:
	// su textura (se lee de su mod, no se copia), misma posición, escala y colores.
	private static final float E3_ESCALA = 0.08527828F;
	private static final int E3_TEX_ANCHO = 2228;
	private static final int E3_TEX_ALTO = 705;
	private static final int E3_RELLENO = 0xFFC81E1E;
	private static final int E3_BRILLO = 0x55FFB347;
	private static final int E3_TEXTO = 0xFFFFE0B0;
	private static final int E3_SEPARACION = Math.round(E3_TEX_ALTO * E3_ESCALA) + 4;

	private void dibujarEstiloExtremo3(final GuiGraphicsExtractor graphics, final Minecraft minecraft, final Collection<LerpingBossEvent> barras) {
		int x = 4;
		int y = 4;
		for (LerpingBossEvent barra : barras) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(x, y);
			graphics.pose().scale(E3_ESCALA, E3_ESCALA);
			graphics.blit(RenderPipelines.GUI_TEXTURED, IntegracionExtremo3.TEXTURA_BARRA, 0, 0, 0.0F, 0.0F, E3_TEX_ANCHO, E3_TEX_ALTO, E3_TEX_ANCHO, E3_TEX_ALTO);
			graphics.pose().popMatrix();
			int barraX = x + Math.round(486.0F * E3_ESCALA);
			int barraY = y + Math.round(276.0F * E3_ESCALA);
			int barraAncho = Math.round(1660.0F * E3_ESCALA);
			int barraAlto = Math.max(2, Math.round(136.0F * E3_ESCALA));
			int relleno = Math.round(barraAncho * Mth.clamp(barra.getProgress(), 0.0F, 1.0F));
			if (relleno > 0) {
				graphics.fill(barraX, barraY, barraX + relleno, barraY + barraAlto, E3_RELLENO);
				graphics.fill(barraX, barraY, barraX + relleno, barraY + Math.max(1, barraAlto / 3), E3_BRILLO);
			}
			graphics.text(minecraft.font, barra.getName(), barraX, y + Math.round(6.0F * E3_ESCALA), E3_TEXTO, true);
			y += E3_SEPARACION;
			if (y > graphics.guiHeight() / 3) {
				break;
			}
		}
	}
}
