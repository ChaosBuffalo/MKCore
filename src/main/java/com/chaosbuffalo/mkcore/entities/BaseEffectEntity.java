package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.WorldAreaEffectEntry;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;

public abstract class BaseEffectEntity extends Entity implements IEntityAdditionalSpawnData {
    protected final List<WorldAreaEffectEntry> effects;
    protected final int WAIT_LAG = 5;
    protected final int DEFAULT_VISUAL_TICK_RATE = 5;
    protected final Map<Entity, Integer> reapplicationDelayMap = Maps.newHashMap();
    protected int duration = 600;
    protected int waitTime = 20;
    protected int tickRate = 5;
    @Nullable
    protected ParticleDisplay particles;
    @Nullable
    protected ParticleDisplay waitingParticles;
    protected LivingEntity owner;
    protected UUID ownerUniqueId;
    private IMKEntityData ownerData;

    public static class ParticleDisplay {
        public enum DisplayType {
            CONTINUOUS,
            ONCE
        }

        protected ResourceLocation particles;
        protected int tickRate;
        protected DisplayType type;

        public ParticleDisplay(ResourceLocation particleName, int tickRate, DisplayType type) {
            particles = particleName;
            this.tickRate = tickRate;
            this.type = type;
        }

        public ResourceLocation getParticles() {
            return particles;
        }

        public DisplayType getType() {
            return type;
        }

        public int getTickRate() {
            return tickRate;
        }

        public boolean shouldTick(int ticksExisted, int offset) {
            switch (type) {
                case ONCE:
                    return ticksExisted - offset == 0;
                case CONTINUOUS:
                default:
                    return ticksExisted % getTickRate() == 0;

            }
        }

        public void write(PacketBuffer buffer) {
            buffer.writeResourceLocation(particles);
            buffer.writeInt(tickRate);
            buffer.writeEnumValue(type);
        }

        public static ParticleDisplay read(PacketBuffer buffer){
            ResourceLocation loc = buffer.readResourceLocation();
            int tickRate = buffer.readInt();
            DisplayType type = buffer.readEnumValue(DisplayType.class);
            return new ParticleDisplay(loc, tickRate, type);
        }
    }

    public BaseEffectEntity(EntityType<? extends BaseEffectEntity> entityType, World world) {
        super(entityType, world);
        this.effects = new ArrayList<>();
        particles = null;
    }

    @Override
    protected void registerData() {

    }

    public void setParticles(ResourceLocation particles) {
        this.particles = new ParticleDisplay(particles, DEFAULT_VISUAL_TICK_RATE, ParticleDisplay.DisplayType.CONTINUOUS);
    }

    public void setWaitingParticles(ResourceLocation waitingParticles) {
        this.waitingParticles = new ParticleDisplay(waitingParticles,
                DEFAULT_VISUAL_TICK_RATE, ParticleDisplay.DisplayType.CONTINUOUS);
    }

    public void setParticles(@Nullable ParticleDisplay display) {
        this.particles = display;
    }

    public void setWaitingParticles(@Nullable ParticleDisplay display) {
        this.waitingParticles = display;
    }


    public void setTickRate(int tickRate) {
        this.tickRate = tickRate;
    }


    public void addEffect(EffectInstance effect, TargetingContext targetContext) {
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

    public void addEffect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isRemote) {
            clientUpdate();
        } else {
            if (serverUpdate()) {
                remove();
            }
        }
    }

    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null){
            anim.spawn(getEntityWorld(), getPositionVec(), null);
        }
    }

    private void clientUpdate() {
        ParticleDisplay display = isWaiting() ? waitingParticles : particles;
        if (display != null && display.shouldTick(ticksExisted, isWaiting() ? 0 : waitTime) ){
            spawnClientParticles(display);
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(owner.getEntityId());
        buffer.writeInt(tickRate);
        buffer.writeInt(waitTime);
        buffer.writeInt(ticksExisted);
        buffer.writeBoolean(particles != null);
        if (particles != null) {
            particles.write(buffer);
        }
        buffer.writeBoolean(waitingParticles != null);
        if (waitingParticles != null) {
            waitingParticles.write(buffer);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity ent = getEntityWorld().getEntityByID(additionalData.readInt());
        if (ent instanceof LivingEntity) {
            owner = (LivingEntity) ent;
        }
        tickRate = additionalData.readInt();
        waitTime = additionalData.readInt();
        ticksExisted = additionalData.readInt();
        boolean hasParticles = additionalData.readBoolean();
        if (hasParticles) {
            particles = ParticleDisplay.read(additionalData);
        }
        boolean hasWaiting = additionalData.readBoolean();
        if (hasWaiting) {
            waitingParticles = ParticleDisplay.read(additionalData);
        }
    }

    public void setOwner(@Nullable LivingEntity ownerIn) {
        this.owner = ownerIn;
        this.ownerUniqueId = ownerIn == null ? null : ownerIn.getUniqueID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUniqueId != null && this.world instanceof ServerWorld) {
            Entity entity = ((ServerWorld)this.world).getEntityByUuid(this.ownerUniqueId);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity)entity;
            }
        }

        return this.owner;
    }

    public boolean isWaiting() {
        return ticksExisted < waitTime;
    }

    @Nullable
    private IMKEntityData getOwnerData() {
        if (ownerData == null) {
            ownerData = MKCore.getEntityDataOrNull(getOwner());
        }
        return ownerData;
    }

    protected abstract Collection<LivingEntity> getEntitiesInBounds();

    private boolean serverUpdate() {
        if (ticksExisted > waitTime + duration + WAIT_LAG + 1) {
            return true;
        }
        IMKEntityData entityData = getOwnerData();
        if (entityData == null)
            return true;

        // lets recalc waiting to include a wait lag so that the server isnt damaging before the client responds
        boolean stillWaiting = ticksExisted <= waitTime + WAIT_LAG;

        if (stillWaiting) {
            return false;
        }

        reapplicationDelayMap.entrySet().removeIf(entry -> ticksExisted >= entry.getValue());

        if (effects.isEmpty()) {
            reapplicationDelayMap.clear();
            return false;
        }

        Collection<LivingEntity> result = getEntitiesInBounds();

        if (result.isEmpty()){
            return false;
        }

        for (LivingEntity target : result){
            reapplicationDelayMap.put(target, ticksExisted + tickRate);
            MKCore.getEntityData(target).ifPresent(targetData ->
                    effects.forEach(entry -> {
                        if (entry.getTickStart() <= ticksExisted - waitTime - WAIT_LAG) {
                            entry.apply(entityData, targetData);
                        }
                    }));
        }


        return false;
    }

    protected boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntityPredicates.NOT_SPECTATING.test(e) &&
                EntityPredicates.IS_LIVING_ALIVE.test(e) &&
                !reapplicationDelayMap.containsKey(e) &&
                e.canBeHitWithPotion();
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected void writeVector(PacketBuffer buffer, Vector3d vector){
        buffer.writeDouble(vector.getX());
        buffer.writeDouble(vector.getY());
        buffer.writeDouble(vector.getZ());
    }

    protected Vector3d readVector(PacketBuffer buffer){
        return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
