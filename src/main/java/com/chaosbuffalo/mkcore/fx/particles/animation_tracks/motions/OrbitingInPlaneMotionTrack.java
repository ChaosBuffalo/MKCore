package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class OrbitingInPlaneMotionTrack extends BaseMotionTrack {

    private double rpm;
    private double rpmVarianceMagnitude;
    private double centralGravity;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.orbit_in_plane");
    private int keyCount = 0;
    private final MKParticle.ParticleDataKey MOTION_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);
    private final MKParticle.ParticleDataKey VARIANCE_SCALAR = new MKParticle.ParticleDataKey(this, keyCount++);


    public OrbitingInPlaneMotionTrack(double rpm, double rpmVarianceMagnitude, double centralGravity){
        super(TYPE_NAME);
        this.rpm = rpm;
        this.rpmVarianceMagnitude = rpmVarianceMagnitude;
        this.centralGravity = centralGravity;
    }

    public OrbitingInPlaneMotionTrack(){
        this(1.0, 0.0, 0.0);
    }


    @Override
    public void begin(MKParticle particle) {
        Vector3d pos = particle.getOrigin().subtract(particle.getPosition());
        particle.setTrackFloatData(VARIANCE_SCALAR, generateVariance(particle));
        Vector3d originVertical = new Vector3d(particle.getOrigin().getX(), particle.getPosition().getY(),
                particle.getOrigin().getZ());
        double realRadius = particle.getPosition().distanceTo(originVertical);
        double startAngleX = Math.acos(pos.getX() / realRadius);
        if (particle.getRand().nextBoolean()){
            startAngleX -= Math.PI;
        }
//        double startAngleZ = Math.asin(pos.getZ() / realRadius);
//        if (particle.getRand().nextBoolean()){
//            startAngleZ -= Math.PI;
//        }
        double w = (Math.PI * 2) / GameConstants.TICKS_PER_SECOND * ((rpm + particle.getTrackFloatData(VARIANCE_SCALAR) * rpmVarianceMagnitude) / 60);
        particle.setTrackVector3dData(MOTION_VECTOR, new Vector3d(startAngleX, w, realRadius));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        rpm = dynamic.get("rpm").asDouble(1.0);
        rpmVarianceMagnitude = dynamic.get("rpmVariance").asDouble(0.0);
        centralGravity = dynamic.get("centralGravity").asDouble(0.0);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        D sup = super.serialize(ops);
        builder.put(ops.createString("rpm"), ops.createDouble(rpm));
        builder.put(ops.createString("rpmVariance"), ops.createDouble(rpmVarianceMagnitude));
        builder.put(ops.createString("centralGravity"), ops.createDouble(centralGravity));
        return ops.mergeToMap(sup, builder.build()).result().orElse(sup);
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        Vector3d variance = particle.getTrackVector3dData(MOTION_VECTOR);
        double vx = -variance.z * Math.sin(variance.x + tick * variance.y);
        double vz = -variance.z * Math.cos(variance.x + tick * variance.y);
        Vector3d desiredPosition = new Vector3d(particle.getOrigin().getX() + vx,
                particle.getPosition().getY(), particle.getOrigin().getZ() + vz);
        Vector3d finalMotion = desiredPosition.subtract(particle.getPosition()).scale(1.0 / GameConstants.TICKS_PER_SECOND);
        if (centralGravity != 0.0f){
            Vector3d toOrigin = particle.getOrigin().subtract(particle.getPosition());
            Vector3d norm = toOrigin.normalize().scale(centralGravity);
            finalMotion = finalMotion.add(norm);
        }
        particle.setMotion(
                MathUtils.lerpDouble(particle.getMotionX(), finalMotion.x, 0.9),
                MathUtils.lerpDouble(particle.getMotionY(), finalMotion.y, 0.9),
                MathUtils.lerpDouble(particle.getMotionZ(), finalMotion.z, 0.9)
        );
//        particle.setMotion(0, 0, 0);
//        particle.setMotion(finalMotion.x, finalMotion.y, finalMotion.z);

    }
}
