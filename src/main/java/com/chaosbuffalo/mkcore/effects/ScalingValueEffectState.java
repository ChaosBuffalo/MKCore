package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public abstract class ScalingValueEffectState extends MKEffectState {
    protected float base = 0.0f;
    protected float scale = 0.0f;
    protected float modScale = 1.0f;
    @Nullable
    protected MKDamageType damageType = null;
    @Nullable
    protected ResourceLocation particles = null;

    public void setScalingParameters(float base, float scale) {
        setScalingParameters(base, scale, 1.0f);
    }

    public void setScalingParameters(float base, float scale, float modScale) {
        this.base = base;
        this.scale = scale;
        this.modScale = modScale;
    }

    public float getScaledValue(int stacks, float skillLevel) {
        return base + (stacks * (scale * skillLevel));
    }

    public float getModifierScale() {
        return modScale;
    }

    public void setDamageType(@Nullable MKDamageType damageType) {
        this.damageType = damageType;
    }

    @Nullable
    public MKDamageType getDamageType() {
        return damageType;
    }

    public void setEffectParticles(ResourceLocation particle) {
        this.particles = particle;
    }

    @Nullable
    public ResourceLocation getParticles() {
        return particles;
    }

    @Override
    public void serializeStorage(CompoundNBT stateTag) {
        super.serializeStorage(stateTag);
        stateTag.putFloat("base", base);
        stateTag.putFloat("scale", scale);
        stateTag.putFloat("modScale", modScale);
        if (damageType != null) {
            MKNBTUtil.writeResourceLocation(stateTag, "damageType", damageType.getId());
        }
        if (particles != null) {
            MKNBTUtil.writeResourceLocation(stateTag, "particles", particles);
        }
    }

    @Override
    public void deserializeStorage(CompoundNBT stateTag) {
        super.deserializeStorage(stateTag);
        base = stateTag.getFloat("base");
        scale = stateTag.getFloat("scale");
        modScale = stateTag.getFloat("modScale");
        if (stateTag.contains("damageType")) {
            damageType = MKCoreRegistry.getDamageType(MKNBTUtil.readResourceLocation(stateTag, "damageType"));
        }
        if (stateTag.contains("particles")) {
            particles = MKNBTUtil.readResourceLocation(stateTag, "particles");
        }
    }

    private final Vector3d YP = new Vector3d(0.0, 1.0, 0.0);

    protected void sendEffectParticles(Entity target) {
        if (particles != null) {
            PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(YP, particles, target.getEntityId()), target);
        }
    }
}
