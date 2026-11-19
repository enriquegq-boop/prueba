package com.extremo.jefes.entity;

import com.extremo.jefes.registry.ModEntities;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Jefe del día 2: el Murciélago Mutante de las minas infectadas.
 *
 * <ul>
 *   <li>Barra de jefe amarilla con su nombre.</li>
 *   <li>Engendra Lepismas Sulfúricos (si te golpean te hunden un bloque hacia abajo).</li>
 *   <li>Mordisco en picado que pega fuerte.</li>
 *   <li>Escupe bolas de azufre.</li>
 *   <li>Por debajo del 50% de vida se enfurece: chillido que empuja, ataca más rápido y engendra más lepismas.</li>
 *   <li>Al morir, todos sus lepismas mueren con él.</li>
 * </ul>
 */
public class MurcielagoMutante extends Monster {
	public static final float VIDA_MAXIMA = 300.0F;
	private static final DustParticleOptions POLVO_AZUFRE = new DustParticleOptions(BolaAzufre.COLOR_AZUFRE, 2.0F);
	private static final double RADIO_ANUNCIO = 96.0;
	private static final double RADIO_LEPISMAS = 48.0;
	private static final EntityDataAccessor<Boolean> DATA_ENFURECIDO = SynchedEntityData.defineId(MurcielagoMutante.class, EntityDataSerializers.BOOLEAN);

	private final ServerBossEvent bossEvent = new ServerBossEvent(
		Mth.createInsecureUUID(this.random), this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10
	);
	public final AnimationState flyAnimationState = new AnimationState();

	private boolean enfurecido;
	private boolean anunciado;
	private int cooldownBola = 60;
	private int cooldownLepismas = 40;
	private int cooldownPicado = 40;
	// Contadores (solo informativos; los usan las pruebas automáticas).
	private int totalBolas;
	private int totalLepismas;
	private int totalMordiscos;

	public MurcielagoMutante(final EntityType<? extends MurcielagoMutante> type, final Level level) {
		super(type, level);
		this.moveControl = new MurcielagoMoveControl(this);
		this.xpReward = 150;
		this.setNoGravity(true);
		this.setPersistenceRequired();
		this.bossEvent.setDarkenScreen(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, VIDA_MAXIMA)
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.ATTACK_DAMAGE, 9.0)
			.add(Attributes.ATTACK_KNOCKBACK, 1.0)
			.add(Attributes.MOVEMENT_SPEED, 0.6)
			.add(Attributes.FLYING_SPEED, 0.6)
			.add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new PicadoGoal());
		this.goalSelector.addGoal(4, new RodearObjetivoGoal());
		this.goalSelector.addGoal(8, new VueloAleatorioGoal());
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 16.0F, 1.0F));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, LepismaSulfurico.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	@Override
	protected void defineSynchedData(final SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_ENFURECIDO, false);
	}

	public boolean isEnfurecido() {
		return this.entityData.get(DATA_ENFURECIDO);
	}

	private void setEnfurecido(final boolean value) {
		this.enfurecido = value;
		this.entityData.set(DATA_ENFURECIDO, value);
		this.bossEvent.setColor(value ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.YELLOW);
	}

	// ------------------------------------------------------------------ tick

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.level().isClientSide()) {
			this.flyAnimationState.startIfStopped(this.tickCount);
			if (this.random.nextInt(4) == 0) {
				this.level().addParticle(POLVO_AZUFRE, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), 0.0, -0.05, 0.0);
			}
		}
	}

	@Override
	protected void customServerAiStep(final ServerLevel level) {
		super.customServerAiStep(level);
		if (!this.anunciado) {
			this.anunciado = true;
			this.anunciar(level, Component.translatable("anuncio.extremo_jefes.murcielago_mutante.despierta"), SoundEvents.WITHER_SPAWN);
			this.anunciar(level, Component.translatable("anuncio.extremo_jefes.lepisma_sulfurico.despierta"), null);
		}

		if (!this.enfurecido && this.getHealth() <= this.getMaxHealth() * 0.5F) {
			this.enfurecerse(level);
		}

		if (this.cooldownBola > 0) {
			this.cooldownBola--;
		}
		if (this.cooldownLepismas > 0) {
			this.cooldownLepismas--;
		}
		if (this.cooldownPicado > 0) {
			this.cooldownPicado--;
		}

		LivingEntity target = this.getTarget();
		if (target != null && target.isAlive()) {
			double distSqr = this.distanceToSqr(target);
			if (this.cooldownBola <= 0 && distSqr > 16.0 && distSqr < 32.0 * 32.0 && this.hasLineOfSight(target)) {
				this.dispararBolas(level, target);
				this.cooldownBola = (this.enfurecido ? 45 : 70) + this.random.nextInt(25);
			}
			if (this.cooldownLepismas <= 0 && distSqr < RADIO_LEPISMAS * RADIO_LEPISMAS) {
				this.engendrarLepismas(level, target, this.enfurecido ? 4 : 2);
				this.cooldownLepismas = (this.enfurecido ? 160 : 220) + this.random.nextInt(40);
			}
		}

		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
	}

	// --------------------------------------------------------------- ataques

	private void dispararBolas(final ServerLevel level, final LivingEntity target) {
		Vec3 origen = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.45, this.getZ());
		Vec3 direccion = target.getEyePosition().subtract(origen).normalize();
		int cantidad = this.enfurecido ? 3 : 1;
		for (int i = 0; i < cantidad; i++) {
			Vec3 dir = direccion;
			if (cantidad > 1) {
				float giro = (i - 1) * 0.18F;
				dir = direccion.yRot(giro);
			}
			BolaAzufre bola = new BolaAzufre(level, this, dir, this.enfurecido ? 8.0F : 7.0F);
			bola.setPos(origen.x + dir.x * 1.6, origen.y, origen.z + dir.z * 1.6);
			level.addFreshEntity(bola);
			this.totalBolas++;
		}
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.5F, 0.6F);
	}

	/** Engendra lepismas junto al objetivo, sin pasar de un máximo vivo a la vez. */
	public int engendrarLepismas(final ServerLevel level, final LivingEntity target, final int cantidad) {
		int maximo = this.enfurecido ? 10 : 6;
		int vivos = this.contarLepismas(level);
		int aEngendrar = Math.min(cantidad, maximo - vivos);
		int engendrados = 0;
		for (int i = 0; i < aEngendrar; i++) {
			BlockPos pos = this.buscarSueloCerca(level, target.blockPosition());
			if (pos == null) {
				continue;
			}
			LepismaSulfurico lepisma = ModEntities.LEPISMA_SULFURICO.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (lepisma == null) {
				continue;
			}
			lepisma.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.random.nextFloat() * 360.0F, 0.0F);
			lepisma.setTarget(target);
			lepisma.setJefe(this.getUUID());
			level.addFreshEntity(lepisma);
			level.sendParticles(POLVO_AZUFRE, lepisma.getX(), lepisma.getY() + 0.2, lepisma.getZ(), 12, 0.3, 0.2, 0.3, 0.02);
			engendrados++;
			this.totalLepismas++;
		}
		if (engendrados > 0) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SILVERFISH_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.5F);
		}
		return engendrados;
	}

	private int contarLepismas(final ServerLevel level) {
		return this.misLepismas(level).size();
	}

	/** Lepismas vivos engendrados por este jefe. */
	public List<LepismaSulfurico> misLepismas(final ServerLevel level) {
		return level.getEntitiesOfClass(
			LepismaSulfurico.class, this.getBoundingBox().inflate(RADIO_LEPISMAS), l -> l.isAlive() && this.getUUID().equals(l.getJefe())
		);
	}

	private @Nullable BlockPos buscarSueloCerca(final ServerLevel level, final BlockPos centro) {
		for (int intento = 0; intento < 10; intento++) {
			int dx = this.random.nextInt(7) - 3;
			int dz = this.random.nextInt(7) - 3;
			for (int dy = 2; dy >= -4; dy--) {
				BlockPos pos = centro.offset(dx, dy, dz);
				if (level.getBlockState(pos).isAir()
					&& level.getBlockState(pos.above()).isAir()
					&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
					return pos;
				}
			}
		}
		return null;
	}

	private void enfurecerse(final ServerLevel level) {
		this.setEnfurecido(true);
		this.anunciar(level, Component.translatable("anuncio.extremo_jefes.murcielago_mutante.enfurecido"), SoundEvents.RAVAGER_ROAR);
		level.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(POLVO_AZUFRE, this.getX(), this.getY(0.5), this.getZ(), 80, 3.0, 2.0, 3.0, 0.1);
		// Chillido: empuja a todos los jugadores cercanos.
		for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0), p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
			Vec3 lejos = player.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
			if (lejos.lengthSqr() < 1.0E-4) {
				lejos = new Vec3(1.0, 0.0, 0.0);
			}
			lejos = lejos.normalize().scale(1.6);
			player.setDeltaMovement(player.getDeltaMovement().add(lejos.x, 0.6, lejos.z));
			player.hurtMarked = true;
		}
		LivingEntity target = this.getTarget();
		if (target != null) {
			this.engendrarLepismas(level, target, 4);
		}
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			this.totalMordiscos++;
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.5F, 0.5F);
		}
		return hit;
	}

	// ----------------------------------------------------------- vida/muerte

	@Override
	public boolean hurtServer(final ServerLevel level, final DamageSource source, final float damage) {
		if (source.getEntity() instanceof LepismaSulfurico || source.getEntity() instanceof MurcielagoMutante) {
			return false;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void die(final DamageSource source) {
		super.die(source);
		if (this.level() instanceof ServerLevel level) {
			this.bossEvent.setProgress(0.0F);
			// "Si lo matamos no sale el lepisma": todos sus lepismas mueren con él.
			for (LepismaSulfurico lepisma : this.misLepismas(level)) {
				lepisma.kill(level);
			}
			this.anunciar(level, Component.translatable("anuncio.extremo_jefes.murcielago_mutante.derrotado"), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
		}
	}

	@Override
	public boolean causeFallDamage(final double fallDistance, final float damageModifier, final DamageSource damageSource) {
		return false;
	}

	@Override
	public void checkDespawn() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
			this.discard();
		} else {
			this.noActionTime = 0;
		}
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false;
	}

	// ------------------------------------------------------------ barra jefe

	@Override
	public void startSeenByPlayer(final ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossEvent.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(final ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	public ServerBossEvent getBossEvent() {
		return this.bossEvent;
	}

	@Override
	public void setCustomName(final @Nullable Component name) {
		super.setCustomName(name);
		this.bossEvent.setName(this.getDisplayName());
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Enfurecido", this.enfurecido);
		output.putBoolean("Anunciado", this.anunciado);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setEnfurecido(input.getBooleanOr("Enfurecido", false));
		this.anunciado = input.getBooleanOr("Anunciado", false);
		if (this.hasCustomName()) {
			this.bossEvent.setName(this.getDisplayName());
		}
	}

	private void anunciar(final ServerLevel level, final Component mensaje, final @Nullable SoundEvent sonido) {
		Component linea = Component.literal("EXTREMO » ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
			.append(mensaje.copy().withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW)));
		AABB zona = this.getBoundingBox().inflate(RADIO_ANUNCIO);
		for (ServerPlayer player : level.getPlayers(p -> zona.contains(p.position()))) {
			player.sendSystemMessage(linea);
			if (sonido != null) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), sonido, SoundSource.HOSTILE, 0.8F, 1.0F);
			}
		}
	}

	// ---------------------------------------------------------------- sonido

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BAT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.BAT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BAT_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.35F + this.random.nextFloat() * 0.1F;
	}

	@Override
	protected float getSoundVolume() {
		return 3.0F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 60;
	}

	// ---------------------------------------------------------- movimiento

	/** Control de vuelo: empuja al murciélago hacia el punto deseado (como el vex, pero choca con los bloques). */
	private static class MurcielagoMoveControl extends MoveControl<MurcielagoMutante> {
		MurcielagoMoveControl(final MurcielagoMutante mob) {
			super(mob);
		}

		@Override
		public void tick() {
			if (this.operation != MoveControl.Operation.MOVE_TO) {
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.9));
				return;
			}
			Vec3 delta = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
			double length = delta.length();
			if (length < 0.6) {
				this.operation = MoveControl.Operation.WAIT;
				this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5));
				return;
			}
			double fuerza = this.speedModifier * (this.mob.isEnfurecido() ? 0.07 : 0.055);
			Vec3 movement = this.mob.getDeltaMovement().add(delta.scale(fuerza / length));
			double max = this.speedModifier * 0.55;
			if (movement.length() > max) {
				movement = movement.normalize().scale(max);
			}
			this.mob.setDeltaMovement(movement);
			LivingEntity target = this.mob.getTarget();
			double lookX = target != null ? target.getX() - this.mob.getX() : movement.x;
			double lookZ = target != null ? target.getZ() - this.mob.getZ() : movement.z;
			this.mob.setYRot(-((float) Mth.atan2(lookX, lookZ)) * (180.0F / (float) Math.PI));
			this.mob.yBodyRot = this.mob.getYRot();
		}
	}

	private boolean espacioLibre(final Vec3 pos) {
		AABB caja = this.getBoundingBox().move(pos.subtract(this.position()));
		return this.level().noCollision(this, caja);
	}

	/** Se lanza en picado contra el objetivo y le muerde. */
	private class PicadoGoal extends Goal {
		private int ticks;
		private boolean mordido;

		PicadoGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = MurcielagoMutante.this.getTarget();
			return target != null
				&& target.isAlive()
				&& MurcielagoMutante.this.cooldownPicado <= 0
				&& MurcielagoMutante.this.distanceToSqr(target) < 28.0 * 28.0
				&& MurcielagoMutante.this.hasLineOfSight(target);
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity target = MurcielagoMutante.this.getTarget();
			return !this.mordido && this.ticks < 80 && target != null && target.isAlive();
		}

		@Override
		public void start() {
			this.ticks = 0;
			this.mordido = false;
			MurcielagoMutante.this.playSound(SoundEvents.PHANTOM_SWOOP, 3.0F, 0.5F);
		}

		@Override
		public void stop() {
			MurcielagoMutante self = MurcielagoMutante.this;
			self.cooldownPicado = (self.enfurecido ? 45 : 80) + self.random.nextInt(40);
			// Tras morder se aleja hacia arriba.
			self.getMoveControl().setWantedPosition(self.getX(), self.getY() + 5.0, self.getZ(), 1.2);
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			MurcielagoMutante self = MurcielagoMutante.this;
			LivingEntity target = self.getTarget();
			if (target == null) {
				return;
			}
			this.ticks++;
			self.getLookControl().setLookAt(target, 30.0F, 30.0F);
			self.getMoveControl().setWantedPosition(target.getX(), target.getY(0.4), target.getZ(), self.enfurecido ? 2.6 : 2.1);
			if (self.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
				self.doHurtTarget(getServerLevel(self), target);
				this.mordido = true;
			}
		}
	}

	/** Vuela en círculos alrededor del objetivo, por encima de él. */
	private class RodearObjetivoGoal extends Goal {
		private int recalcular;
		private Vec3 punto = Vec3.ZERO;

		RodearObjetivoGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = MurcielagoMutante.this.getTarget();
			return target != null && target.isAlive();
		}

		@Override
		public void start() {
			this.recalcular = 0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			MurcielagoMutante self = MurcielagoMutante.this;
			LivingEntity target = self.getTarget();
			if (target == null) {
				return;
			}
			self.getLookControl().setLookAt(target, 30.0F, 30.0F);
			if (--this.recalcular <= 0 || self.position().distanceToSqr(this.punto) < 2.0) {
				this.recalcular = 30 + self.random.nextInt(20);
				this.punto = this.elegirPunto(target);
			}
			self.getMoveControl().setWantedPosition(this.punto.x, this.punto.y, this.punto.z, 1.0);
		}

		private Vec3 elegirPunto(final LivingEntity target) {
			MurcielagoMutante self = MurcielagoMutante.this;
			for (int intento = 0; intento < 8; intento++) {
				double angulo = self.random.nextDouble() * Math.PI * 2.0;
				double radio = 5.0 + self.random.nextDouble() * 4.0;
				for (double altura = 5.0 + self.random.nextDouble() * 2.0; altura >= 1.0; altura -= 1.0) {
					Vec3 candidato = new Vec3(target.getX() + Math.cos(angulo) * radio, target.getY() + altura, target.getZ() + Math.sin(angulo) * radio);
					if (self.espacioLibre(candidato)) {
						return candidato;
					}
				}
			}
			return new Vec3(target.getX(), target.getY() + 2.0, target.getZ());
		}
	}

	/** Sin objetivo: revolotea cerca de donde está. */
	private class VueloAleatorioGoal extends Goal {
		VueloAleatorioGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return MurcielagoMutante.this.getTarget() == null
				&& !MurcielagoMutante.this.getMoveControl().hasWanted()
				&& MurcielagoMutante.this.random.nextInt(reducedTickDelay(10)) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			MurcielagoMutante self = MurcielagoMutante.this;
			for (int i = 0; i < 4; i++) {
				Vec3 candidato = self.position().add(self.random.nextInt(15) - 7, self.random.nextInt(7) - 3, self.random.nextInt(15) - 7);
				if (self.espacioLibre(candidato)) {
					self.getMoveControl().setWantedPosition(candidato.x, candidato.y, candidato.z, 0.5);
					return;
				}
			}
		}
	}

	public int getTotalBolas() {
		return this.totalBolas;
	}

	public int getTotalLepismas() {
		return this.totalLepismas;
	}

	public int getTotalMordiscos() {
		return this.totalMordiscos;
	}

	/** Usado por el test para comprobar cuántos jugadores ven la barra. */
	public List<ServerPlayer> getJugadoresConBarra() {
		return List.copyOf(this.bossEvent.getPlayers());
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected boolean canRide(final Entity vehicle) {
		return false;
	}

	public static boolean esJefe(final Mob mob) {
		return mob instanceof MurcielagoMutante;
	}
}
