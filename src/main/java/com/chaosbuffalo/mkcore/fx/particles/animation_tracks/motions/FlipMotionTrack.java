package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class FlipMotionTrack extends BaseMotionTrack {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.flip_motion");

    public FlipMotionTrack() {
        super(TYPE_NAME);
    }

    @Override
    public FlipMotionTrack copy() {
        return new FlipMotionTrack();
    }

    @Override
    public void apply(MKParticle particle) {
        super.apply(particle);
        Vector3d newMotion = particle.getMotion().scale(-1);
        particle.setMotion(newMotion.getX(), newMotion.getY(), newMotion.getZ());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {

    }
}
