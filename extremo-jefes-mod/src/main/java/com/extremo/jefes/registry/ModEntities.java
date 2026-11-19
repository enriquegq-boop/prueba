package com.extremo.jefes.registry;

import com.extremo.jefes.ExtremoJefes;
import com.extremo.jefes.entity.BolaAzufre;
import com.extremo.jefes.entity.LepismaSulfurico;
import com.extremo.jefes.entity.MurcielagoMutante;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<MurcielagoMutante> MURCIELAGO_MUTANTE = register(
		"murcielago_mutante",
		EntityType.Builder.of(MurcielagoMutante::new, MobCategory.MONSTER)
			.sized(2.6F, 2.8F)
			.eyeHeight(1.7F)
			.clientTrackingRange(10)
	);

	public static final EntityType<LepismaSulfurico> LEPISMA_SULFURICO = register(
		"lepisma_sulfurico",
		EntityType.Builder.of(LepismaSulfurico::new, MobCategory.MONSTER)
			.sized(0.4F, 0.3F)
			.eyeHeight(0.13F)
			.clientTrackingRange(8)
	);

	public static final EntityType<BolaAzufre> BOLA_AZUFRE = register(
		"bola_azufre",
		EntityType.Builder.<BolaAzufre>of(BolaAzufre::new, MobCategory.MISC)
			.noLootTable()
			.sized(0.6F, 0.6F)
			.clientTrackingRange(6)
			.updateInterval(5)
	);

	private ModEntities() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ExtremoJefes.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(MURCIELAGO_MUTANTE, MurcielagoMutante.createAttributes());
		FabricDefaultAttributeRegistry.register(LEPISMA_SULFURICO, LepismaSulfurico.createAttributes());
	}
}
