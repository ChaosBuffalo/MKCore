package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class BrownianMotionTrack extends ParticleMotionAnimationTrack {
    protected final IntAttribute tickInterval = new IntAttribute("tickInterval", 5);
    protected final FloatAttribute magnitude = new FloatAttribute("magnitude", 1.0f);
    protected final BooleanAttribute doX = new BooleanAttribute("doX", true);
    protected final BooleanAttribute doY = new BooleanAttribute("doY", true);
    protected final BooleanAttribute doZ = new BooleanAttribute("doZ", true);
    protected final BooleanAttribute doGravity = new BooleanAttribute("doGravity", true);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.brownian_motion");
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public BrownianMotionTrack(int tickInterval, float magnitude){
        this();
        this.tickInterval.setValue(tickInterval);
        this.magnitude.setValue(magnitude);
    }

    public BrownianMotionTrack(){
        super(TYPE_NAME);
        addAttributes(tickInterval, magnitude, doX, doY, doZ, doGravity);
    }

    public BrownianMotionTrack disableX(){
        this.doX.setValue(false);
        return this;
    }

    public BrownianMotionTrack disableY(){
        this.doY.setValue(false);
        return this;
    }

    public BrownianMotionTrack disableZ(){
        this.doZ.setValue(false);
        return this;
    }

    public BrownianMotionTrack withGravity(boolean value){
        this.doGravity.setValue(value);
        return this;
    }

    @Override
    public BrownianMotionTrack copy() {
        BrownianMotionTrack copy = new BrownianMotionTrack(tickInterval.value(), magnitude.getValue()).withGravity(doGravity.getValue());
        if (!doX.getValue()){
            copy.disableX();
        }
        if (!doY.getValue()){
            copy.disableY();
        }
        if (!doZ.getValue()){
            copy.disableZ();
        }
        return copy;
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        if (tick % tickInterval.value() == 0){
            double motionX = doX.getValue() ? generateVariance(particle) * magnitude.getValue() : particle.getMotionX();
            double motionY = doY.getValue() ? generateVariance(particle) * magnitude.getValue() : particle.getMotionY();
            double motionZ = doZ.getValue() ? generateVariance(particle) * magnitude.getValue() : particle.getMotionZ();
            if (doGravity.getValue()){
                motionY += particle.getParticleGravity() * tickInterval.value();
            }
            particle.setTrackVector3dData(VARIANCE_VECTOR, new Vector3d(motionX, motionY, motionZ));
        }
        float tickTime = Math.min(1.0f, ((tick % tickInterval.value()) + 1.0f) / tickInterval.value());
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
