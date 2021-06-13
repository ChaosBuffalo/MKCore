package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.visual_attributes.ParticleColorAttribute;
import com.chaosbuffalo.mkcore.fx.particles.visual_attributes.ParticleRenderScaleAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;


public class ParticleKeyFrame {
    protected final ParticleColorAttribute EMPTY_COLOR = new ParticleColorAttribute();
    protected final ParticleRenderScaleAttribute EMPTY_SCALE = new ParticleRenderScaleAttribute();
    protected ParticleColorAttribute color;
    protected ParticleRenderScaleAttribute scale;
    protected final int tickStart;
    protected final int tickEnd;
    protected final int duration;

    public ParticleKeyFrame(int tickStart, int duration){
        this.tickStart = tickStart;
        this.tickEnd = tickStart + duration;
        this.duration = duration;
    }

    public ParticleKeyFrame(){
        this(0, 0);
    }

    public ParticleKeyFrame withColor(float red, float green, float blue){
        setColorAttribute(new ParticleColorAttribute(red, green, blue));
        return this;
    }

    public ParticleKeyFrame withScale(float scale, float variance){
        setScaleAttribute(new ParticleRenderScaleAttribute(scale, variance));
        return this;
    }

    public int getTickStart() {
        return tickStart;
    }

    public ParticleRenderScaleAttribute getScale() {
        return hasScaleAttribute() ? scale : EMPTY_SCALE;
    }

    public int getTickEnd() {
        return tickEnd;
    }

    public int getDuration() {
        return duration;
    }

    public float getInterpolationTime(int currentTick){
        return MathUtils.clamp((float) (currentTick - tickStart) / getDuration(), 0.0f, 1.0f);
    }

    public void setColorAttribute(ParticleColorAttribute color){
        this.color = color;
    }

    public boolean hasColorAttribute(){
        return this.color != null;
    }

    public boolean hasScaleAttribute(){
        return this.scale != null;
    }

    public void setScaleAttribute(ParticleRenderScaleAttribute scale){
        this.scale = scale;
    }

    public void apply(MKParticle particle){
        if (hasColorAttribute()){
            color.apply(particle);
        }
        if (hasScaleAttribute()){
            scale.apply(particle);
        }
    }

    public void begin(MKParticle particle){
        if (hasScaleAttribute()){
            particle.pushVariance(scale);
        }
        if (duration == 0){
            apply(particle);
            end(particle);
        }
    }


    public ParticleColorAttribute getColor(){
        return hasColorAttribute() ? color : EMPTY_COLOR;
    }

    public void end(MKParticle particle){
        if (hasColorAttribute()){
            particle.getCurrentFrame().setColorAttribute(getColor());
        }
        if (hasScaleAttribute()){
            particle.getCurrentFrame().setScaleAttribute(getScale());
        }
    }

    public void animate(MKParticle particle, int currentTick, float partialTicks){
        float t = getInterpolationTime(currentTick) + partialTicks / getDuration();
        if (hasColorAttribute()){
            getColor().animate(particle, t);
        }
        if (hasScaleAttribute()){
            getScale().animate(particle ,t);
        }
    }


}
