package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class BrownianMotionTrack extends ParticleMotionAnimationTrack {
    private int tickInterval;
    private float magnitude;
    private boolean doX;
    private boolean doY;
    private boolean doZ;
    private boolean doGravity;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.brownian_motion");
    private int keyCount = 0;
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public BrownianMotionTrack(int tickInterval, float magnitude){
        super(TYPE_NAME);
        this.tickInterval = tickInterval;
        this.magnitude = magnitude;
        this.doX = true;
        this.doY = true;
        this.doGravity = true;
        this.doZ = true;
    }

    public BrownianMotionTrack(){
        this(5, 1.0f);
    }

    public BrownianMotionTrack disableX(){
        this.doX = false;
        return this;
    }

    public BrownianMotionTrack disableY(){
        this.doY = false;
        return this;
    }

    public BrownianMotionTrack disableZ(){
        this.doZ = false;
        return this;
    }

    public BrownianMotionTrack withGravity(boolean value){
        this.doGravity = value;
        return this;
    }


    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        tickInterval = dynamic.get("tickInterval").asInt(5);
        magnitude = dynamic.get("magnitude").asFloat(1.0f);
        doX = dynamic.get("doX").asBoolean(true);
        doY = dynamic.get("doY").asBoolean(true);
        doZ = dynamic.get("doZ").asBoolean(true);
        doGravity = dynamic.get("doGravity").asBoolean(true);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        D sup = super.serialize(ops);
        builder.put(ops.createString("tickInterval"), ops.createInt(tickInterval));
        builder.put(ops.createString("magnitude"), ops.createFloat(magnitude));
        builder.put(ops.createString("doX"), ops.createBoolean(doX));
        builder.put(ops.createString("doY"), ops.createBoolean(doY));
        builder.put(ops.createString("doZ"), ops.createBoolean(doZ));
        builder.put(ops.createString("doGravity"), ops.createBoolean(doGravity));
        return ops.mergeToMap(sup, builder.build()).result().orElse(sup);
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        if (tick % tickInterval == 0){
            double motionX = doX ? generateVariance(particle) * magnitude : particle.getMotionX();
            double motionY = doY ? generateVariance(particle) * magnitude : particle.getMotionY();
            double motionZ = doZ ? generateVariance(particle) * magnitude : particle.getMotionZ();
            if (doGravity){
                motionY += particle.getParticleGravity() * tickInterval;
            }
            particle.setTrackVector3dData(VARIANCE_VECTOR, new Vector3d(motionX, motionY, motionZ));
        }
        float tickTime = Math.min(1.0f, ((tick % tickInterval) + 1.0f) / tickInterval);
        Vector3d goalVec = particle.getTrackVector3dData(VARIANCE_VECTOR);
        particle.setMotion(
                MathUtils.lerpDouble(particle.getMotionX(), goalVec.x, tickTime),
                MathUtils.lerpDouble(particle.getMotionY(), goalVec.y, tickTime),
                MathUtils.lerpDouble(particle.getMotionZ(), goalVec.z, tickTime)
        );
    }

    @Override
    public Vector3d getMotion(MKParticle particle) {
        return particle.getTrackVector3dData(VARIANCE_VECTOR);
    }
}
