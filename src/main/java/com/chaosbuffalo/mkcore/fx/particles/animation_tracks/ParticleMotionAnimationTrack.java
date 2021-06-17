package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ParticleMotionAnimationTrack extends ParticleAnimationTrack {

    public ParticleMotionAnimationTrack(ResourceLocation typeName) {
        super(typeName);
    }

    public abstract Vector3d getMotion(MKParticle particle);

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setMotionTrack(this);
    }
}
