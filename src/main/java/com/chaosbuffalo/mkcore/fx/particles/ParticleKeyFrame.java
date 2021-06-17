package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.*;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.InheritMotionTrack;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;


public class ParticleKeyFrame {
    protected final ParticleColorAnimationTrack EMPTY_COLOR = new ParticleColorAnimationTrack();
    protected final ParticleRenderScaleAnimationTrack EMPTY_SCALE = new ParticleRenderScaleAnimationTrack();
    protected final InheritMotionTrack EMPTY_MOTION = new InheritMotionTrack();
    protected ParticleColorAnimationTrack colorTrack;
    protected ParticleRenderScaleAnimationTrack scaleTrack;
    protected ParticleMotionAnimationTrack motionTrack;
    protected int tickStart;
    protected int tickEnd;
    protected int duration;

    public ParticleKeyFrame(int tickStart, int duration){
        this.tickStart = tickStart;
        this.tickEnd = tickStart + duration;
        this.duration = duration;
    }

    public ParticleKeyFrame(){
        this(0, 0);
    }

    public ParticleKeyFrame withColor(float red, float green, float blue){
        setColorTrack(new ParticleColorAnimationTrack(red, green, blue));
        return this;
    }

    public ParticleKeyFrame withMotion(ParticleMotionAnimationTrack motion){
        setMotionTrack(motion);
        return this;
    }

    public ParticleKeyFrame withScale(float scale, float variance){
        setScaleTrack(new ParticleRenderScaleAnimationTrack(scale, variance));
        return this;
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("tickStart"), ops.createInt(tickStart));
        builder.put(ops.createString("duration"), ops.createInt(duration));
        if (hasMotionTrack()){
            builder.put(ops.createString("motionTrack"), motionTrack.serialize(ops));
        }
        if (hasScaleTrack()){
            builder.put(ops.createString("scaleTrack"), scaleTrack.serialize(ops));
        }
        if (hasColorTrack()){
            builder.put(ops.createString("colorTrack"), colorTrack.serialize(ops));
        }
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        tickStart = dynamic.get("tickStart").asInt(0);
        duration = dynamic.get("duration").asInt(0);
        tickEnd = tickStart + duration;
        motionTrack = (ParticleMotionAnimationTrack) dynamic.get("motionTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleMotionAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return null;
            }
        }).result().orElse(null);
        colorTrack = (ParticleColorAnimationTrack) dynamic.get("colorTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleColorAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return null;
            }
        }).result().orElse(null);
        scaleTrack = (ParticleRenderScaleAnimationTrack) dynamic.get("scaleTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleRenderScaleAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return null;
            }
        }).result().orElse(null);
    }

    public boolean hasMotionTrack(){
        return this.motionTrack != null;
    }

    public void setMotionTrack(ParticleMotionAnimationTrack motionAttribute) {
        this.motionTrack = motionAttribute;
    }

    public int getTickStart() {
        return tickStart;
    }

    public ParticleRenderScaleAnimationTrack getScaleTrack() {
        return hasScaleTrack() ? scaleTrack : EMPTY_SCALE;
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

    public void setColorTrack(ParticleColorAnimationTrack color){
        this.colorTrack = color;
    }

    public boolean hasColorTrack(){
        return this.colorTrack != null;
    }

    public boolean hasScaleTrack(){
        return this.scaleTrack != null;
    }

    public void setScaleTrack(ParticleRenderScaleAnimationTrack scale){
        this.scaleTrack = scale;
    }

    public ParticleMotionAnimationTrack getMotionTrack() {
        return hasMotionTrack() ? motionTrack : EMPTY_MOTION;
    }

    public void apply(MKParticle particle){
        if (hasColorTrack()){
            colorTrack.apply(particle);
        }
        if (hasScaleTrack()){
            scaleTrack.apply(particle);
        }
        if (hasMotionTrack()){
            motionTrack.apply(particle);
        }
    }

    public void begin(MKParticle particle){
        if (hasScaleTrack()){
            getScaleTrack().begin(particle);
        }
        if (hasMotionTrack()){
            getMotionTrack().begin(particle);
        }
        if (hasColorTrack()){
            getColorTrack().begin(particle);
        }
        if (getDuration() == 0){
            apply(particle);
            end(particle);
        }
    }


    public ParticleColorAnimationTrack getColorTrack(){
        return hasColorTrack() ? colorTrack : EMPTY_COLOR;
    }

    public void end(MKParticle particle){
        if (hasColorTrack()){
            getColorTrack().end(particle);
        }
        if (hasScaleTrack()){
            getScaleTrack().end(particle);
        }
        if (hasMotionTrack()){
            getMotionTrack().end(particle);
        }
    }

    public void animate(MKParticle particle, int currentTick, float partialTicks){
        float t = getInterpolationTime(currentTick) + partialTicks / getDuration();
        if (hasColorTrack()){
            getColorTrack().animate(particle, t, currentTick - tickStart);
        }
        if (hasScaleTrack()){
            getScaleTrack().animate(particle, t, currentTick - tickStart);
        }
        if (hasMotionTrack()){
            getMotionTrack().animate(particle, t, currentTick - tickStart);
        }
    }

    public void update(MKParticle particle, int currentTick){
        if (hasMotionTrack()){
            getMotionTrack().update(particle, currentTick - tickStart, getInterpolationTime(currentTick));
        }
        if (hasColorTrack()){
            getColorTrack().update(particle, currentTick - tickStart, getInterpolationTime(currentTick));
        }
        if (hasScaleTrack()){
            getScaleTrack().update(particle, currentTick - tickStart, getInterpolationTime(currentTick));
        }
    }


}
