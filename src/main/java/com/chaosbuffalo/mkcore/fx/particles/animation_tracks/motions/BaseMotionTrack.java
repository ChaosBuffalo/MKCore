package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public abstract class BaseMotionTrack extends ParticleMotionAnimationTrack {
    public BaseMotionTrack(ResourceLocation typeName) {
        super(typeName);
    }

    @Override
    public Vector3d getMotion(MKParticle particle) {
        return particle.getMotion();
    }
}
