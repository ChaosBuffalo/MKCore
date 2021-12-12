package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class LinearMotionTrack extends ParticleMotionAnimationTrack {
    protected final DoubleAttribute xSpeed = new DoubleAttribute("xSpeed", 0.0f);
    protected final DoubleAttribute ySpeed = new DoubleAttribute("ySpeed", 0.0f);
    protected final DoubleAttribute zSpeed = new DoubleAttribute("zSpeed", 0.0f);
    protected final DoubleAttribute varianceMagnitude = new DoubleAttribute("varianceMagnitude", 0.0f);
    private Vector3d motionVec;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.linear_motion");
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public LinearMotionTrack(double xSpeed, double ySpeed, double zSpeed, double varianceMagnitude){
        this();
        this.xSpeed.setValue(xSpeed);
        this.ySpeed.setValue(ySpeed);
        this.zSpeed.setValue(zSpeed);
        this.motionVec = new Vector3d(xSpeed, ySpeed, zSpeed);
        this.varianceMagnitude.setValue(varianceMagnitude);
    }

    public LinearMotionTrack(){
        super(TYPE_NAME);
        addAttributes(xSpeed, ySpeed, zSpeed, varianceMagnitude);
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        particle.setTrackVector3dData(VARIANCE_VECTOR,
                new Vector3d(generateVariance(particle), generateVariance(particle), generateVariance(particle)));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        super.deserialize(dynamic);
        motionVec = new Vector3d(xSpeed.getValue(), ySpeed.getValue(), zSpeed.getValue());
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setMotion(xSpeed.getValue(), ySpeed.getValue(), zSpeed.getValue());
    }

    @Override
    public LinearMotionTrack copy() {
        return new LinearMotionTrack(xSpeed.getValue(), ySpeed.getValue(), zSpeed.getValue(), varianceMagnitude.getValue());
    }

    @Override
    public Vector3d getMotion(MKParticle particle) {
        return motionVec.add(particle.getTrackVector3dData(VARIANCE_VECTOR).scale(varianceMagnitude.getValue()));
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
