package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.modules.particles.OctoRenderManager;
import it.hurts.octostudios.octolib.modules.particles.trail.TrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class LifeEssenceEntity extends ThrowableProjectile implements ITargetableEntity, TrailProvider {
    private static final EntityDataAccessor<Float> HEAL = SynchedEntityData.defineId(LifeEssenceEntity.class, EntityDataSerializers.FLOAT);

    public void setHeal(float heal) {
        this.getEntityData().set(HEAL, heal);
    }

    public float getHeal() {
        return this.getEntityData().get(HEAL);
    }

    private LivingEntity target;

    public LifeEssenceEntity(EntityType<? extends LifeEssenceEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        var level = this.getCommandSenderWorld();

        level.addParticle((ParticleUtils.constructSimpleSpark(new Color(200 + random.nextInt(50), 150 + random.nextInt(50), random.nextInt(50)), 0.05F, 10, 0.9F)), this.xOld, this.yOld, this.zOld,
                -this.getDeltaMovement().x * 0.1F * random.nextFloat(), -this.getDeltaMovement().y * 0.1F * random.nextFloat(), -this.getDeltaMovement().z * 0.1F * random.nextFloat());

        if (target == null) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        if (this.position().distanceTo(target.getEyePosition()) > 1F) {
            var direction = target.getEyePosition().subtract(this.position()).normalize();
            var motion = this.getDeltaMovement();

            var window = 5;

            if (tickCount > window) {
                var factor = Math.clamp((tickCount - window) * 0.1F, 0F, 1F);

                this.setDeltaMovement(motion.x + (direction.x * factor - motion.x) * factor, motion.y + (direction.y * factor - motion.y) * factor, motion.z + (direction.z * factor - motion.z) * factor);
            }
        } else {
            target.heal(getHeal());

            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HEAL, 0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("heal", getHeal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setHeal(tag.getFloat("heal"));
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        OctoRenderManager.registerProvider(this);
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.target = target;

        if (!level().isClientSide() && target != null)
            NetworkHandler.sendToClientsTrackingEntity(new S2CEntityTargetPacket(this.getId(), target.getId()), this);
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
        return tickCount > 2;
    }

    @Override
    public int getTrailMaxLength() {
        return 5;
    }

    @Override
    public int getTrailFadeInColor() {
        return 0xFFFFFF00;
    }

    @Override
    public int getTrailFadeOutColor() {
        return 0x80FF0000;
    }

    @Override
    public double getTrailScale() {
        return 0.025F;
    }
}