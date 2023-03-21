package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class PointEffectEntity extends BaseEffectEntity{
    @ObjectHolder(MKCore.MOD_ID + ":mk_point_effect")
    public static EntityType<PointEffectEntity> TYPE;
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(PointEffectEntity.class, EntityDataSerializers.FLOAT);

    public PointEffectEntity(EntityType<? extends PointEffectEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Nonnull
    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
    }

    public void setRadius(float radiusIn) {
        if (!this.level.isClientSide) {
            this.getEntityData().set(RADIUS, radiusIn);
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (RADIUS.equals(key)) {
            this.refreshDimensions();
//            this.recenterBoundingBox();
            this.setBoundingBox(this.dimensions.makeBoundingBox(getX(), getY()-getRadius(), getZ()));
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
    }

//    @Override
//    public void writeSpawnData(PacketBuffer buffer) {
//        super.writeSpawnData(buffer);
//        buffer.writeFloat(getRadius());
//    }
//
//    @Override
//    public void readSpawnData(PacketBuffer additionalData) {
//        super.readSpawnData(additionalData);
//        setRadius(additionalData.readFloat());
//    }

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(RADIUS, 1.0F);
    }

    public PointEffectEntity(Level worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPos(x, y, z);
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return this.level.getLoadedEntitiesOfClass(LivingEntity.class,
                getBoundingBox(), this::entityCheck);
    }
}
