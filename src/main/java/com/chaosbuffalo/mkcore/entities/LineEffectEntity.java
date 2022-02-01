package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.WorldAreaEffectEntry;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.utils.EntityCollectionRayTraceResult;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LineEffectEntity extends Entity implements IEntityAdditionalSpawnData {
    @ObjectHolder(MKCore.MOD_ID + ":mk_line_effect")
    public static EntityType<LineEffectEntity> TYPE;

    private final List<WorldAreaEffectEntry> effects;
    protected int duration = 600;
    protected int waitTime = 20;
    protected int tickRate = 5;
    protected int visualTickRate = 5;
    private static final DataParameter<Boolean> WAITING = EntityDataManager.createKey(LineEffectEntity.class, DataSerializers.BOOLEAN);

    @Nullable
    private ResourceLocation particles;

    @Nullable
    private ResourceLocation waitingParticles;

    private Vector3d startPoint;
    private Vector3d endPoint;

    protected LivingEntity owner;
    protected UUID ownerUniqueId;
    private IMKEntityData ownerData;

    public LineEffectEntity(EntityType<? extends LineEffectEntity> entityType, World world) {
        super(entityType, world);
        this.effects = new ArrayList<>();
        particles = null;
    }

    public LineEffectEntity(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
    }

    public void setStartPoint(Vector3d startPoint) {
        this.startPoint = startPoint;
    }

    public void setParticles(@Nullable ResourceLocation particles) {
        this.particles = particles;
    }

    public void setWaitingParticles(@Nullable ResourceLocation waitingParticles) {
        this.waitingParticles = waitingParticles;
    }

    public void setEndPoint(Vector3d endPoint) {
        this.endPoint = endPoint;
    }

    public void setTickRate(int tickRate) {
        this.tickRate = tickRate;
    }

    public void setVisualTickRate(int visualTickRate) {
        this.visualTickRate = visualTickRate;
    }

    @Override
    protected void registerData() {
        this.getDataManager().register(WAITING, false);
    }

    public void addEffect(EffectInstance effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
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

    private void clientUpdate() {
        if (ticksExisted % visualTickRate == 0){
            ResourceLocation animName = isWaiting() ? waitingParticles : particles;
            if (animName != null){
                ParticleAnimation anim = ParticleAnimationManager.getAnimation(animName);
                if (anim != null){
                    anim.spawn(getEntityWorld(), startPoint, Collections.singletonList(endPoint));
                }
            }
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
        return this.getDataManager().get(WAITING);
    }

    private boolean isInWaitPhase() {
        return isWaiting();
    }

    protected void setWaiting(boolean waiting) {
        this.getDataManager().set(WAITING, waiting);
    }

    private void setInWaitPhase(boolean waitPhase) {
        setWaiting(waitPhase);
    }

    @Nullable
    private IMKEntityData getOwnerData() {
        if (ownerData == null) {
            ownerData = MKCore.getEntityDataOrNull(getOwner());
        }
        return ownerData;
    }

    private boolean serverUpdate() {
        if (ticksExisted > waitTime + duration) {
            return true;
        }
        IMKEntityData entityData = getOwnerData();
        if (entityData == null)
            return true;

        boolean stillWaiting = ticksExisted <= waitTime;

        if (isInWaitPhase() != stillWaiting) {
            setInWaitPhase(stillWaiting);
        }

        if (stillWaiting) {
            return false;
        }

        if (ticksExisted % tickRate != 0) {
            return false;
        }

        EntityCollectionRayTraceResult<LivingEntity> result = RayTraceUtils.rayTraceAllEntities(
                LivingEntity.class, getEntityWorld(),
                startPoint, endPoint, Vector3d.ZERO,
                1.5f, 0.0f, this::entityCheck);

        if (result.getEntities().isEmpty()){
            return false;
        }

        for (EntityCollectionRayTraceResult.TraceEntry<LivingEntity> target : result.getEntities()){
            MKCore.getEntityData(target.entity).ifPresent(targetData ->
                    effects.forEach(entry -> entry.apply(entityData, targetData)));
        }


        return false;
    }

    private boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntityPredicates.NOT_SPECTATING.test(e) &&
                EntityPredicates.IS_LIVING_ALIVE.test(e) &&
                e.canBeHitWithPotion();
    }


    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(owner.getEntityId());
        writeVector(buffer, startPoint);
        writeVector(buffer, endPoint);
        buffer.writeInt(visualTickRate);
        buffer.writeInt(tickRate);
        buffer.writeBoolean(particles != null);
        if (particles != null) {
            buffer.writeResourceLocation(particles);
        }
        buffer.writeBoolean(waitingParticles != null);
        if (waitingParticles != null) {
            buffer.writeResourceLocation(waitingParticles);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity ent = getEntityWorld().getEntityByID(additionalData.readInt());
        if (ent instanceof LivingEntity) {
            owner = (LivingEntity) ent;
        }
        startPoint = readVector(additionalData);
        endPoint = readVector(additionalData);
        visualTickRate = additionalData.readInt();
        tickRate = additionalData.readInt();
        boolean hasParticles = additionalData.readBoolean();
        if (hasParticles) {
            particles = additionalData.readResourceLocation();
        }
        boolean hasWaiting = additionalData.readBoolean();
        if (hasWaiting) {
            waitingParticles = additionalData.readResourceLocation();
        }
    }

    private void writeVector(PacketBuffer buffer, Vector3d vector){
        buffer.writeDouble(vector.getX());
        buffer.writeDouble(vector.getY());
        buffer.writeDouble(vector.getZ());
    }

    private Vector3d readVector(PacketBuffer buffer){
        return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
