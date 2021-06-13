package com.chaosbuffalo.mkcore.fx.particles.visual_attributes;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;

import java.util.Random;

public interface IParticleAttribute {


    void apply(MKParticle particle);

    void animate(MKParticle particle, float time);

    default float generateVariance(MKParticle particle, Random rand){
        return (rand.nextFloat() * 2.0f) - 1.0f;
    }
}
