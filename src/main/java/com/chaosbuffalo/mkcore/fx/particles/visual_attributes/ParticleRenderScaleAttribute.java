package com.chaosbuffalo.mkcore.fx.particles.visual_attributes;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.utils.MathUtils;

public class ParticleRenderScaleAttribute implements IParticleAttribute {
    private final float renderScale;
    private final float maxVariance;


    public ParticleRenderScaleAttribute(float scale, float maxVariance){
        this.renderScale = scale;
        this.maxVariance = maxVariance;
    }

    public ParticleRenderScaleAttribute(){
        this(1.0f, 0.0f);
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setScale(getScaleWithVariance(particle));
    }

    protected float getScaleWithVariance(MKParticle particle){
        return renderScale + (particle.getVarianceForAttribute(this) * maxVariance);
    }

    @Override
    public void animate(MKParticle particle, float time) {
        particle.setScale(MathUtils.lerp(particle.getCurrentFrame().getScale().getScaleWithVariance(particle),
                getScaleWithVariance(particle), time));
    }
}
