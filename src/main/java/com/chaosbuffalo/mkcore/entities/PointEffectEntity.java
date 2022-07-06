package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.Collection;

public class PointEffectEntity extends BaseEffectEntity{
    @ObjectHolder(MKCore.MOD_ID + ":mk_point_effect")
    public static EntityType<PointEffectEntity> TYPE;
    private static final DataParameter<Float> RADIUS = EntityDataManager.createKey(PointEffectEntity.class, DataSerializers.FLOAT);

    public PointEffectEntity(EntityType<? extends PointEffectEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        return EntitySize.flexible(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
    }

    public void setRadius(float radiusIn) {
        if (!this.world.isRemote) {
            this.getDataManager().set(RADIUS, radiusIn);
        }

    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (RADIUS.equals(key)) {
            this.recalculateSize();
//            this.recenterBoundingBox();
            this.setBoundingBox(this.size.func_242285_a(getPosX(), getPosY()-getRadius(), getPosZ()));
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    public void recalculateSize() {
        super.recalculateSize();
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
        return this.getDataManager().get(RADIUS);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.getDataManager().register(RADIUS, 1.0F);
    }

    public PointEffectEntity(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return this.world.getLoadedEntitiesWithinAABB(LivingEntity.class,
                getBoundingBox(), this::entityCheck);
    }
}
