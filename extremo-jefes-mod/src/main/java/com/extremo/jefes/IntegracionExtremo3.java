package com.extremo.jefes;

import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/**
 * Integración con el mod "Extremo3" (mobs sulfúricos, de WasterTV).
 * No se copia nada de ese mod: solo se usan sus mobs y su textura de barra por su nombre
 * cuando está instalado. Si no está, el jefe funciona igual con su barra propia y sin lepismas.
 */
public final class IntegracionExtremo3 {
	public static final String MOD_ID = "extremo3";
	public static final Identifier LEPISMA_SULFURICO = Identifier.fromNamespaceAndPath(MOD_ID, "lepisma_sulfurico");
	/** Textura de la barra de jefe de Extremo3 (la que usan su dragón y su piglin mutante). */
	public static final Identifier TEXTURA_BARRA = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/backgroundboss.png");

	private IntegracionExtremo3() {
	}

	public static boolean cargado() {
		return FabricLoader.getInstance().isModLoaded(MOD_ID);
	}

	public static Optional<EntityType<?>> tipoLepisma() {
		if (!cargado()) {
			return Optional.empty();
		}
		return BuiltInRegistries.ENTITY_TYPE.getOptional(LEPISMA_SULFURICO);
	}
}
