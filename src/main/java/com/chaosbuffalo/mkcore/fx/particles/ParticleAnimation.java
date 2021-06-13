package com.chaosbuffalo.mkcore.fx.particles;

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

    public void tickAnimation(MKParticle particle, float partialTicks){
        for (ParticleKeyFrame frame : keyFrames){
            if (frame.getTickStart() == particle.getAge()){
                frame.begin(particle);
            }
            if (frame.getDuration() > 0 && particle.getAge() >= frame.getTickStart() && particle.getAge() < frame.getTickEnd()){
                frame.animate(particle, particle.getAge(), partialTicks);
            }
            if (particle.getAge() == frame.getTickEnd() - 1){
                frame.end(particle);
            }
        }
    }


}
