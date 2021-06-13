package com.chaosbuffalo.mkcore.fx.particles.visual_attributes;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.utils.MathUtils;

public class ParticleColorAttribute implements IParticleAttribute{
    protected final float red;
    protected final float green;
    protected final float blue;

    public ParticleColorAttribute(float red, float green, float blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ParticleColorAttribute(){
        this(1.0f, 1.0f, 1.0f);
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setColor(red, green, blue);
    }

    @Override
    public void animate(MKParticle particle, float time) {
        ParticleColorAttribute current = particle.getCurrentFrame().getColor();
        particle.setColor(
                MathUtils.lerp(current.red, red, time),
                MathUtils.lerp(current.green, green, time),
                MathUtils.lerp(current.blue, blue, time)
        );
    }
}
