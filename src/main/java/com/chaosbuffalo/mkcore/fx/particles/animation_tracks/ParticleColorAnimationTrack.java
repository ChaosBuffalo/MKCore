package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public abstract class ParticleColorAnimationTrack extends ParticleAnimationTrack{

    public ParticleColorAnimationTrack(ResourceLocation typeName) {
        super(typeName, AnimationTrackType.COLOR);
    }

    public abstract Vector3f getColor(MKParticle particle);

    @Override
    public abstract ParticleColorAnimationTrack copy();

    protected float getColorWithVariance(float color, float varianceMagnitude, float variance){
        return Math.max(0.0f, Math.min(1.0f, color + varianceMagnitude * variance));
    }

    @Override
    public void apply(MKParticle particle) {
        Vector3f color = getColor(particle);
        particle.setColor(color.getX(), color.getY(), color.getZ());
    }

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setColorTrack(this);
    }


}
