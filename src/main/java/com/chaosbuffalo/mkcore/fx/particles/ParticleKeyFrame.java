package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.*;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors.ParticleLerpColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors.ParticleStaticColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.InheritMotionTrack;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.SimpleAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ParticleKeyFrame implements ISerializableAttributeContainer {
    protected final ParticleColorAnimationTrack EMPTY_COLOR = new ParticleStaticColorAnimationTrack();
    protected final ParticleRenderScaleAnimationTrack EMPTY_SCALE = new ParticleRenderScaleAnimationTrack();
    protected final InheritMotionTrack EMPTY_MOTION = new InheritMotionTrack();
    protected ParticleColorAnimationTrack colorTrack;
    protected ParticleRenderScaleAnimationTrack scaleTrack;
    protected ParticleMotionAnimationTrack motionTrack;
    protected final List<ISerializableAttribute<?>> attributes;
    protected final IntAttribute tickStart = new IntAttribute("tickStart", 0);
    protected final IntAttribute duration = new IntAttribute("duration", 0);
    protected int tickEnd;

    public ParticleKeyFrame(int tickStart, int duration){
        this();
        this.tickStart.setValue(tickStart);
        this.duration.setValue(duration);
    }

    public ParticleKeyFrame(){
        attributes = new ArrayList<>();
        tickStart.setOnSetCallback(this::onSetStartOrDuration);
        duration.setOnSetCallback(this::onSetStartOrDuration);
        addAttributes(tickStart, duration);
        tickEnd = tickStart.getValue() + duration.getValue();
    }

    private void onSetStartOrDuration(SimpleAttribute<Integer> attribute){
        tickEnd = tickStart.getValue() + duration.getValue();
    }

    public ParticleKeyFrame withColor(float red, float green, float blue){
        setColorTrack(new ParticleLerpColorAnimationTrack(red, green, blue));
        return this;
    }

    public ParticleKeyFrame withMotion(ParticleMotionAnimationTrack motion){
        setMotionTrack(motion);
        return this;
    }

    public void deleteTrack(ParticleAnimationTrack.AnimationTrackType trackType){
        switch (trackType){
            case MOTION:
                setMotionTrack(null);
                return;
            case SCALE:
                setScaleTrack(null);
                return;
            case COLOR:
                setColorTrack(null);
        }
    }

    public ParticleKeyFrame withScale(float scale, float variance){
        setScaleTrack(new ParticleRenderScaleAnimationTrack(scale, variance));
        return this;
    }

    public ParticleKeyFrame copy(){
        ParticleKeyFrame copy = new ParticleKeyFrame(tickStart.getValue(), duration.getValue());
        if (hasColorTrack()){
            copy.setColorTrack(colorTrack.copy());
        }
        if (hasMotionTrack()){
            copy.setMotionTrack(motionTrack.copy());
        }
        if (hasScaleTrack()){
            copy.setScaleTrack(scaleTrack.copy());
        }
        return copy;
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
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
        Map<String, Dynamic<D>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
        motionTrack = (ParticleMotionAnimationTrack) dynamic.get("motionTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleMotionAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return EMPTY_MOTION.copy();
            }
        }).result().orElse(null);
        colorTrack = (ParticleColorAnimationTrack) dynamic.get("colorTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleColorAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return EMPTY_COLOR.copy();
            }
        }).result().orElse(null);
        scaleTrack = (ParticleRenderScaleAnimationTrack) dynamic.get("scaleTrack").map(d -> {
            ResourceLocation type = ParticleAnimationTrack.getType(d);
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(type);
            if (track instanceof ParticleRenderScaleAnimationTrack){
                track.deserialize(d);
                return track;
            } else {
                return EMPTY_SCALE.copy();
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
        return tickStart.getValue();
    }

    public ParticleRenderScaleAnimationTrack getScaleTrack() {
        return hasScaleTrack() ? scaleTrack : EMPTY_SCALE;
    }

    public int getTickEnd() {
        return tickEnd;
    }

    public int getDuration() {
        return duration.getValue();
    }

    public float getInterpolationTime(int currentTick, float partialTicks){
        return MathUtils.clamp(((float) (currentTick - tickStart.getValue()) + partialTicks) / getDuration(), 0.0f, 1.0f);
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
            getScaleTrack().begin(particle, getDuration());
        }
        if (hasMotionTrack()){
            getMotionTrack().begin(particle, getDuration());
        }
        if (hasColorTrack()){
            getColorTrack().begin(particle, getDuration());
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
        float t = getInterpolationTime(currentTick, partialTicks);
        if (hasColorTrack()){
            getColorTrack().animate(particle, t, currentTick - tickStart.getValue(), getDuration(), partialTicks);
        }
        if (hasScaleTrack()){
            getScaleTrack().animate(particle, t, currentTick - tickStart.getValue(), getDuration(), partialTicks);
        }
        if (hasMotionTrack()){
            getMotionTrack().animate(particle, t, currentTick - tickStart.getValue(), getDuration(), partialTicks);
        }
    }

    public void update(MKParticle particle, int currentTick){
        float time = getInterpolationTime(currentTick, 0.0f);
        if (hasMotionTrack()){
            getMotionTrack().update(particle, currentTick - tickStart.getValue(), time);
        }
        if (hasColorTrack()){
            getColorTrack().update(particle, currentTick - tickStart.getValue(), time);
        }
        if (hasScaleTrack()){
            getScaleTrack().update(particle, currentTick - tickStart.getValue(), time);
        }
    }

    @Override
    public String toString() {
        return "ParticleKeyFrame{" +
                "tickStart=" + tickStart.getValue() +
                ", tickEnd=" + tickEnd +
                ", duration=" + duration.getValue() +
                '}';
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> attribute) {
        attributes.add(attribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
    }
}