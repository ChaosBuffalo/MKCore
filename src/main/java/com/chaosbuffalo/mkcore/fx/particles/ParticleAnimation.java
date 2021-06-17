package com.chaosbuffalo.mkcore.fx.particles;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.List;

public class ParticleAnimation {

    public List<ParticleKeyFrame> keyFrames;

    public ParticleAnimation(){
        this.keyFrames = new ArrayList<>();
    }

    public void addKeyFrame(ParticleKeyFrame frame){
        this.keyFrames.add(frame);
    }

    public ParticleAnimation withKeyFrame(ParticleKeyFrame frame){
        addKeyFrame(frame);
        return this;
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("frames"),
                ops.createList(keyFrames.stream().map(frame -> frame.serialize(ops))));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        List<ParticleKeyFrame> newFrames = dynamic.get("frames").asList(d -> {
            ParticleKeyFrame frame = new ParticleKeyFrame();
            frame.deserialize(d);
            return frame;
        });
        keyFrames.clear();
        keyFrames.addAll(newFrames);
    }

    public void tick(MKParticle particle){
        for (ParticleKeyFrame frame : keyFrames){
            if (frame.getTickStart() == particle.getAge()){
                frame.begin(particle);
            }
            if (frame.getDuration() > 0 && particle.getAge() >= frame.getTickStart() && particle.getAge() < frame.getTickEnd()){
                frame.update(particle, particle.getAge());
            }
            if (particle.getAge() == frame.getTickEnd() - 1){
                frame.end(particle);
            }
        }
    }

    public void tickAnimation(MKParticle particle, float partialTicks){
        for (ParticleKeyFrame frame : keyFrames){
            if (frame.getDuration() > 0 && particle.getAge() >= frame.getTickStart() && particle.getAge() < frame.getTickEnd()){
                frame.animate(particle, particle.getAge(), partialTicks);
            }
        }
    }


}
