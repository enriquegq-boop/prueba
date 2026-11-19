package com.extremo.jefes.entity;

import com.extremo.jefes.registry.ModEntities;
import com.extremo.jefes.registry.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Bola de azufre que dispara el Murciélago Mutante. No prende fuego ni rompe bloques:
 * hace daño, envenena un poco y salpica azufre.
 */
public class BolaAzufre extends Fireball {
	public static final int COLOR_AZUFRE = 0xE8D23A;
	private static final DustParticleOptions TRAIL = new DustParticleOptions(COLOR_AZUFRE, 1.4F);
	private float damage = 7.0F;

	public BolaAzufre(final EntityType<? extends BolaAzufre> type, final Level level) {
		super(type, level);
		this.setItem(new ItemStack(ModItems.BOLA_AZUFRE));
	}

	public BolaAzufre(final Level level, final LivingEntity owner, final Vec3 direction, final float damage) {
		super(ModEntities.BOLA_AZUFRE, owner, direction, level);
		this.damage = damage;
		this.setItem(new ItemStack(ModItems.BOLA_AZUFRE));
	}

	@Override
	protected void readAdditionalSaveData(final net.minecraft.world.level.storage.ValueInput input) {
		super.readAdditionalSaveData(input);
		// Con /summon sin datos, Fireball pondría una carga ígnea: siempre es una bola de azufre.
		if (!this.getItem().is(ModItems.BOLA_AZUFRE)) {
			this.setItem(new ItemStack(ModItems.BOLA_AZUFRE));
		}
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	protected @Nullable ParticleOptions getTrailParticle() {
		return TRAIL;
	}

	@Override
	protected boolean canHitEntity(final Entity entity) {
		if (entity instanceof MurcielagoMutante) {
			return false;
		}
		return super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(final EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity target = hitResult.getEntity();
			LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
			DamageSource source = this.damageSources().mobProjectile(this, owner);
			if (target.hurtServer(serverLevel, source, this.damage) && target instanceof LivingEntity livingTarget) {
				livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
			}
		}
	}

	@Override
	protected void onHit(final HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(TRAIL, this.getX(), this.getY(), this.getZ(), 30, 0.6, 0.6, 0.6, 0.05);
			serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2F, 0.6F);
			this.discard();
		}
	}
}
