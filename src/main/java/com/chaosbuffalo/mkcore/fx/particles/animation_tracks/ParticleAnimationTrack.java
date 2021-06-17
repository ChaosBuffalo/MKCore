package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ParticleAnimationTrack {
    private final ResourceLocation typeName;
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "particle_anim.invalid");

    public ParticleAnimationTrack(ResourceLocation typeName){
        this.typeName = typeName;
    }

    public void apply(MKParticle particle) {

    }

    public void animate(MKParticle particle, float time, int trackTick){

    }

    public void end(MKParticle particle){

    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public void begin(MKParticle particle){

    }

    public <D> D serialize(DynamicOps<D> ops){
        return ops.createMap(ImmutableMap.of(
                ops.createString("trackType"), ops.createString(getTypeName().toString())
        ));
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("trackType").asString().result().orElse(INVALID_OPTION.toString()));
    }

    public abstract <D> void deserialize(Dynamic<D> dynamic);

    public float generateVariance(MKParticle particle){
        return (particle.getRand().nextFloat() * 2.0f) - 1.0f;
    }

    public Vector3d generateVarianceVector(MKParticle particle){
        return new Vector3d(generateVariance(particle), generateVariance(particle), generateVariance(particle));
    }

    public void update(MKParticle particle, int tick, float time) {

    }
}
