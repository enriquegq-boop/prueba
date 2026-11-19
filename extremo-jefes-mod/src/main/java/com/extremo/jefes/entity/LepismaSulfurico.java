package com.extremo.jefes.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Lepisma Sulfúrico: "pequeños parásitos engendrados por el temible murciélago mutante.
 * Habilidad especial: si te golpea, te teletransportará un bloque hacia abajo."
 */
public class LepismaSulfurico extends Silverfish {
	private static final DustParticleOptions POLVO = new DustParticleOptions(BolaAzufre.COLOR_AZUFRE, 1.0F);
	/** Murciélago Mutante que lo engendró (null si se puso con huevo o comando). */
	private @Nullable UUID jefe;

	public LepismaSulfurico(final EntityType<? extends LepismaSulfurico> type, final Level level) {
		super(type, level);
		this.xpReward = 3;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.FOLLOW_RANGE, 24.0);
	}

	public @Nullable UUID getJefe() {
		return this.jefe;
	}

	public void setJefe(final @Nullable UUID jefe) {
		this.jefe = jefe;
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.storeNullable("Jefe", UUIDUtil.CODEC, this.jefe);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		this.jefe = input.read("Jefe", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	protected void registerGoals() {
		// Sin los goals de la lepisma normal (no se esconde en la piedra ni despierta a otras).
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, MurcielagoMutante.class, LepismaSulfurico.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living && living.isAlive()) {
			hundirUnBloque(level, living);
		}
		return hit;
	}

	/** Teletransporta al objetivo un bloque hacia abajo (salvo bedrock/bloques irrompibles o fuera del mundo). */
	public static boolean hundirUnBloque(final ServerLevel level, final LivingEntity target) {
		if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
			return false;
		}
		BlockPos below = target.blockPosition().below();
		if (below.getY() <= level.getMinY()) {
			return false;
		}
		BlockState state = level.getBlockState(below);
		if (!state.isAir() && state.getDestroySpeed(level, below) < 0.0F) {
			return false;
		}
		// Nunca entierra la cabeza: si al bajar los ojos quedarían dentro de un bloque, no hunde
		// (así varios golpes seguidos no te dejan asfixiándote bajo tierra).
		double nuevaY = target.getY() - 1.0;
		BlockPos ojos = BlockPos.containing(target.getX(), nuevaY + target.getEyeHeight(), target.getZ());
		if (level.getBlockState(ojos).isSuffocating(level, ojos)) {
			return false;
		}
		target.teleportTo(target.getX(), nuevaY, target.getZ());
		level.sendParticles(POLVO, target.getX(), target.getY() + 0.5, target.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
		level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.HOSTILE, 0.7F, 0.6F);
		return true;
	}
}
