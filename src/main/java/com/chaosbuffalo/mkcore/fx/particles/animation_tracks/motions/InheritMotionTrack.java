package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;

public class InheritMotionTrack extends BaseMotionTrack {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.particle_motion");

    public InheritMotionTrack() {
        super(TYPE_NAME);
    }

    @Override
    public InheritMotionTrack copy() {
        return new InheritMotionTrack();
    }
}
