package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LineEffectEntity extends BaseEffectEntity {
    @ObjectHolder(MKCore.MOD_ID + ":mk_line_effect")
    public static EntityType<LineEffectEntity> TYPE;
    private Vector3d startPoint;
    private Vector3d endPoint;

    public LineEffectEntity(EntityType<? extends LineEffectEntity> entityType, World world) {
        super(entityType, world);
    }

    public LineEffectEntity(World worldIn, double x, double y, double z) {
        this(TYPE, worldIn);
        this.setPosition(x, y, z);
    }

    public void setStartPoint(Vector3d startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(Vector3d endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return RayTraceUtils.rayTraceAllEntities(LivingEntity.class, getEntityWorld(),
                startPoint, endPoint, Vector3d.ZERO,
                1.5f, 0.0f, this::entityCheck).getEntities().stream().map(x -> x.entity)
                .collect(Collectors.toList());
    }

    @Override
    protected void spawnClientParticles() {
        ResourceLocation animName = isWaiting() ? waitingParticles : particles;
        if (animName != null){
            ParticleAnimation anim = ParticleAnimationManager.getAnimation(animName);
            if (anim != null){
                anim.spawn(getEntityWorld(), startPoint, Collections.singletonList(endPoint));
            }
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        writeVector(buffer, startPoint);
        writeVector(buffer, endPoint);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        startPoint = readVector(additionalData);
        endPoint = readVector(additionalData);
    }
}
