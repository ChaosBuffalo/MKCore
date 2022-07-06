package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MKAreaEffectEntity extends AreaEffectCloudEntity implements IEntityAdditionalSpawnData {
    @ObjectHolder(MKCore.MOD_ID + ":mk_area_effect")
    public static EntityType<MKAreaEffectEntity> TYPE;

    private static final float DEFAULT_RADIUS = 3.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;

    private final List<WorldAreaEffectEntry> effects;
    private boolean particlesDisabled;
    private IMKEntityData ownerData;


    public MKAreaEffectEntity(EntityType<? extends AreaEffectCloudEntity> entityType, World world) {
        super(entityType, world);
        this.particlesDisabled = false;
        effects = new ArrayList<>();
        setRadius(DEFAULT_RADIUS);
    }

    public MKAreaEffectEntity(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
    }

    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        return EntitySize.flexible(this.getRadius() * 2.0F, DEFAULT_HEIGHT);
    }

    public void setPeriod(int delay) {
        this.reapplicationDelay = delay;
    }

    public void disableParticle() {
        // TODO
        particlesDisabled = true;
    }

    private boolean isInWaitPhase() {
        return shouldIgnoreRadius();
    }

    private void setInWaitPhase(boolean waitPhase) {
        setIgnoreRadius(waitPhase);
    }

    private void entityTick() {
        // We don't want to call AreaEffectCloudEntity.tick because it'll do all the logic. This is what Entity.tick() does
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.baseTick();
    }

    @Override
    public void tick() {
        entityTick();

        if (this.world.isRemote()) {
            if (!particlesDisabled) {
                clientUpdate();
            }
        } else {
            if (serverUpdate()) {
                remove();
            }
        }
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("ParticlesDisabled", particlesDisabled);
    }

    @Override
    protected void readAdditional(@Nonnull CompoundNBT compound) {
        super.readAdditional(compound);
        particlesDisabled = compound.getBoolean("ParticlesDisabled");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(particlesDisabled);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        particlesDisabled = additionalData.readBoolean();
    }

    public void addEffect(EffectInstance effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    public void addEffect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    public void addDelayedEffect(EffectInstance effect, TargetingContext targetContext, int delayTicks) {
        WorldAreaEffectEntry entry = WorldAreaEffectEntry.forEffect(this, effect, targetContext);
        entry.setTickStart(delayTicks);
        this.effects.add(entry);
    }

    public void addDelayedEffect(MKEffectBuilder<?> effect, TargetingContext targetContext, int delayTicks) {
        WorldAreaEffectEntry entry = WorldAreaEffectEntry.forEffect(this, effect, targetContext);
        entry.setTickStart(delayTicks);
        this.effects.add(entry);

    }

    private boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntityPredicates.NOT_SPECTATING.test(e) &&
                EntityPredicates.IS_LIVING_ALIVE.test(e) &&
                !reapplicationDelayMap.containsKey(e) &&
                e.canBeHitWithPotion();
    }

    @Nullable
    private IMKEntityData getOwnerData() {
        if (ownerData == null) {
            ownerData = MKCore.getEntityDataOrNull(getOwner());
        }
        return ownerData;
    }

    private boolean serverUpdate() {
        if (ticksExisted >= waitTime + duration) {
            return true;
        }

        IMKEntityData entityData = getOwnerData();
        if (entityData == null)
            return true;

        boolean stillWaiting = ticksExisted < waitTime;

        if (isInWaitPhase() != stillWaiting) {
            setInWaitPhase(stillWaiting);
        }

        if (stillWaiting) {
            return false;
        }

        // TODO: FUTURE: see if this can be made dynamic by inspecting the effects
        if (ticksExisted % 5 != 0) {
            return false;
        }

        reapplicationDelayMap.entrySet().removeIf(entry -> ticksExisted >= entry.getValue());

        if (effects.isEmpty()) {
            reapplicationDelayMap.clear();
            return false;
        }

        // Copy in case callbacks try to add more effects
        List<WorldAreaEffectEntry> targetEffects = new ArrayList<>(effects);
        List<LivingEntity> potentialTargets = this.world.getLoadedEntitiesWithinAABB(LivingEntity.class,
                getBoundingBox(), this::entityCheck);
        if (potentialTargets.isEmpty()) {
            return false;
        }

        float radius = getRadius();
        float maxRange = radius * radius;
        for (LivingEntity target : potentialTargets) {

            double d0 = target.getPosX() - getPosX();
            double d1 = target.getPosZ() - getPosZ();
            double entityDist = d0 * d0 + d1 * d1;

            if (entityDist > maxRange) {
                continue;
            }

            reapplicationDelayMap.put(target, ticksExisted + reapplicationDelay);
            MKCore.getEntityData(target).ifPresent(targetData ->
                    targetEffects.forEach(entry -> {
                        if (entry.getTickStart() <= ticksExisted - waitTime) {
                            entry.apply(entityData, targetData);
                        }
                    }));
        }
        return false;
    }

    private void clientUpdate() {
        if (ticksExisted % 5 != 0) {
            return;
        }
        IParticleData particle = this.getParticleData();

        if (isInWaitPhase()) {
            if (!rand.nextBoolean()) {
                return;
            }

            for (int i = 0; i < 2; i++) {
                float f1 = rand.nextFloat() * ((float) Math.PI * 2F);
                float f2 = MathHelper.sqrt(rand.nextFloat()) * 0.2F;
                float xOff = MathHelper.cos(f1) * f2;
                float zOff = MathHelper.sin(f1) * f2;

                if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                    int color = rand.nextBoolean() ? 16777215 : getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    world.addOptionalParticle(particle, getPosX() + xOff, getPosY(), getPosZ() + zOff, r / 255f, g / 255f, b / 255f);
                } else {
                    world.addOptionalParticle(particle, getPosX() + xOff, getPosY(), getPosZ() + zOff, 0, 0, 0);
                }
            }
        } else {
            float radius = getRadius();
            int particleCount = (int) radius * 10;

            for (int i = 0; i < particleCount; i++) {
                float f6 = rand.nextFloat() * ((float) Math.PI * 2F);
                float f7 = MathHelper.sqrt(rand.nextFloat()) * radius;
                float xOffset = MathHelper.cos(f6) * f7;
                float zOffset = MathHelper.sin(f6) * f7;

                if (particle == ParticleTypes.ENTITY_EFFECT) {
                    int color = getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, r / 255f, g / 255f, b / 255f);
                } else if (particle == ParticleTypes.NOTE) {
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, rand.nextInt(24) / 24.0f, 0.009999999776482582D, (0.5D - rand.nextDouble()) * 0.15D);
                } else {
                    world.addOptionalParticle(particle, getPosX() + xOffset, getPosY(), getPosZ() + zOffset, (0.5D - rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - rand.nextDouble()) * 0.15D);
                }
            }
        }
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}