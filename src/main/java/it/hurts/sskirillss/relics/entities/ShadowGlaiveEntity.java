package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.modules.particles.OctoRenderManager;
import it.hurts.octostudios.octolib.modules.particles.trail.TrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShadowGlaiveEntity extends ThrowableProjectile implements ITargetableEntity, TrailProvider {
    private static final EntityDataAccessor<Integer> MAX_BOUNCES = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOUNCES = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CHANCE = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.FLOAT);

    @Getter
    private Set<String> bouncedTargets = new HashSet<>();

    @Nullable
    private LivingEntity currentTarget = null;
    @Nullable
    private LivingEntity lastTarget = null;

    public ShadowGlaiveEntity(EntityType<? extends ShadowGlaiveEntity> type, Level level) {
        super(type, level);
    }

    public void setMaxBounces(int maxBounces) {
        this.getEntityData().set(MAX_BOUNCES, maxBounces);
    }

    public int getMaxBounces() {
        return this.getEntityData().get(MAX_BOUNCES);
    }

    public void setBounces(int bounces) {
        this.getEntityData().set(BOUNCES, bounces);
    }

    public int getBounces() {
        return this.getEntityData().get(BOUNCES);
    }

    public void addBounces(int bounces) {
        setBounces(Math.clamp(getBounces() + bounces, 0, getMaxBounces()));
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setChance(float chance) {
        this.getEntityData().set(CHANCE, chance);
    }

    public float getChance() {
        return this.getEntityData().get(CHANCE);
    }

    public List<LivingEntity> locateNearestTargets() {
        return this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16D)).stream()
                .sorted(Comparator.comparing(entity -> entity.position().distanceTo(this.position())))
                .filter(entity -> (lastTarget == null || !lastTarget.getStringUUID().equals(entity.getStringUUID()))
                        && !entity.isDeadOrDying()
                        && entity.hasLineOfSight(this)
                        && (!(this.getOwner() instanceof Player player) || !EntityUtils.isAlliedTo(player, entity))
                        && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
                .toList();
    }

    @Override
    public void tick() {
        super.tick();

        var level = getCommandSenderWorld();

        var particleCenter = this.position().add(this.getDeltaMovement().scale(-1F));

        for (int i = 0; i < 5; i++)
            level.addParticle(ParticleUtils.constructSimpleSpark(new Color(50 + random.nextInt(100), 0, 150 + random.nextInt(100)), 0.1F + random.nextFloat() * 0.15F, 5 + random.nextInt(10), 0.85F),
                    particleCenter.x() + MathUtils.randomFloat(random) * 0.25F, particleCenter.y(), particleCenter.z() + MathUtils.randomFloat(random) * 0.25F, 0F, 0F, 0F);

        var currentTarget = getTarget();

        if (currentTarget != null && (this.position().distanceTo(currentTarget.position()) >= 16F || currentTarget.isDeadOrDying()))
            currentTarget = null;

        if (!level.isClientSide()) {
            LivingEntity potentialTarget = null;

            var candidateEntities = locateNearestTargets();

            var targetEntities = candidateEntities.stream()
                    .filter(entity -> !bouncedTargets.contains(entity.getStringUUID()))
                    .toList();

            if (!targetEntities.isEmpty())
                potentialTarget = targetEntities.getFirst();
            else if (!candidateEntities.isEmpty()) {
                bouncedTargets.clear();

                potentialTarget = candidateEntities.getFirst();
            }

            if (potentialTarget != null && (currentTarget == null || !currentTarget.getStringUUID().equals(potentialTarget.getStringUUID()))) {
                NetworkHandler.sendToClientsTrackingEntity(new S2CEntityTargetPacket(this.getId(), potentialTarget.getId()), this);

                setTarget(potentialTarget);

                currentTarget = potentialTarget;
            }
        }

        if (currentTarget == null || currentTarget.isDeadOrDying() || this.tickCount >= 250 || getBounces() >= getMaxBounces()) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        if (this.getEyePosition().distanceTo(currentTarget.getEyePosition()) <= 1.5F) {
            currentTarget.invulnerableTime = 0;

            if (currentTarget.hurt(level.damageSources().thrown(this, getOwner()), getDamage())) {
                bouncedTargets.add(currentTarget.getStringUUID());
                lastTarget = currentTarget;

                setTarget(null);
                addBounces(1);

                if (random.nextDouble() <= getChance()) {
                    var entity = new ShadowGlaiveEntity(EntityRegistry.SHADOW_GLAIVE.get(), level);

                    entity.setMaxBounces(getMaxBounces());
                    entity.setBounces(getBounces());
                    entity.setPos(getEyePosition());
                    entity.setDamage(getDamage());
                    entity.setOwner(getOwner());

                    level.addFreshEntity(entity);
                }
            }
        } else {
            this.setDeltaMovement(currentTarget.getEyePosition().subtract(this.getEyePosition()).normalize());
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        OctoRenderManager.registerProvider(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MAX_BOUNCES, 10);
        builder.define(BOUNCES, 0);
        builder.define(DAMAGE, 1F);
        builder.define(CHANCE, -1F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("max_bounces", getMaxBounces());
        tag.putInt("bounces", getBounces());
        tag.putFloat("damage", getDamage());
        tag.putFloat("chance", getChance());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setMaxBounces(tag.getInt("max_bounces"));
        setBounces(tag.getInt("bounces"));
        setDamage(tag.getFloat("damage"));
        setChance(tag.getFloat("chance"));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return currentTarget;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.currentTarget = target;
    }

    @Override
    public Vec3 getTrailPosition(float partialTicks) {
        return getPosition(partialTicks).add(getDeltaMovement().scale(-1));
    }

    @Override
    public int getTrailUpdateFrequency() {
        return 1;
    }

    @Override
    public boolean isTrailAlive() {
        return isAlive();
    }

    @Override
    public boolean isTrailGrowing() {
        return getKnownMovement().length() >= 0.1F;
    }

    @Override
    public int getTrailMaxLength() {
        return 5;
    }

    @Override
    public int getTrailFadeInColor() {
        return 0xFFFF00FF;
    }

    @Override
    public int getTrailFadeOutColor() {
        return 0x800000FF;
    }

    @Override
    public double getTrailScale() {
        return 0.15F;
    }
}