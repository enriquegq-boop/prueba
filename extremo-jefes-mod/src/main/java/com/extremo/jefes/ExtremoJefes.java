package com.extremo.jefes;

import com.extremo.jefes.registry.ModEntities;
import com.extremo.jefes.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtremoJefes implements ModInitializer {
	public static final String MOD_ID = "extremo_jefes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModEntities.init();
		ModItems.init();
		LOGGER.info("Extremo Jefes cargado: Murciélago Mutante listo");
	}
}
