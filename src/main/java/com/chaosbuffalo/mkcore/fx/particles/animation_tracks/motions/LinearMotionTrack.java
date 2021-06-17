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

public class LinearMotionTrack extends ParticleMotionAnimationTrack {
    private double xSpeed;
    private double ySpeed;
    private double zSpeed;
    private Vector3d motionVec;
    private double varianceMagnitude;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.linear_motion");
    private int keyCount = 0;
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public LinearMotionTrack(double xSpeed, double ySpeed, double zSpeed, double varianceMagnitude){
        super(TYPE_NAME);
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.motionVec = new Vector3d(xSpeed, ySpeed, zSpeed);
        this.varianceMagnitude = varianceMagnitude;
    }

    public LinearMotionTrack(){
        this(0.0, 0.0, 0.0, 0.0);
    }

    @Override
    public void begin(MKParticle particle) {
        particle.setTrackVector3dData(VARIANCE_VECTOR,
                new Vector3d(generateVariance(particle), generateVariance(particle), generateVariance(particle)));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        xSpeed = dynamic.get("xSpeed").asDouble(0.0);
        ySpeed = dynamic.get("ySpeed").asDouble(0.0);
        zSpeed = dynamic.get("zSpeed").asDouble(0.0);
        varianceMagnitude = dynamic.get("varianceMagnitude").asDouble(0.0);
        motionVec = new Vector3d(xSpeed, ySpeed, zSpeed);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        D sup = super.serialize(ops);
        builder.put(ops.createString("xSpeed"), ops.createDouble(xSpeed));
        builder.put(ops.createString("ySpeed"), ops.createDouble(ySpeed));
        builder.put(ops.createString("zSpeed"), ops.createDouble(zSpeed));
        builder.put(ops.createString("varianceMagnitude"), ops.createDouble(varianceMagnitude));
        return ops.mergeToMap(sup, builder.build()).result().orElse(sup);
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setMotion(xSpeed, ySpeed, zSpeed);
    }

    @Override
    public Vector3d getMotion(MKParticle particle) {
        return motionVec.add(particle.getTrackVector3dData(VARIANCE_VECTOR).scale(varianceMagnitude));
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        Vector3d currentMotion = particle.getCurrentFrame().getMotionTrack().getMotion(particle);
        Vector3d desiredMotion = getMotion(particle);
        particle.setMotion(
                MathUtils.lerpDouble(currentMotion.x, desiredMotion.x, time),
                MathUtils.lerpDouble(currentMotion.y, desiredMotion.y, time),
                MathUtils.lerpDouble(currentMotion.z, desiredMotion.z, time)
        );
    }

}
