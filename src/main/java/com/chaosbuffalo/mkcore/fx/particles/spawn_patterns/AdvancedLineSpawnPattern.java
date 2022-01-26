package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.joml.AxisAngle4d;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdvancedLineSpawnPattern extends ParticleSpawnPattern{
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.advanced_line");
    protected final DoubleAttribute offset = new DoubleAttribute("offset", 0.0);
    protected final DoubleAttribute motion = new DoubleAttribute("motion", 0.0);
    protected final IntAttribute perPosCount = new IntAttribute("per_pos_count", 10);
    protected final Vector3d EMPTY_VEC = new Vector3d(0.0, 0.0, 0.0);

    public AdvancedLineSpawnPattern(){
        super(TYPE);
        addAttributes(offset, motion, perPosCount);
    }

    public AdvancedLineSpawnPattern(double offset, double motion, int perPosCount){
        this();
        this.offset.setValue(offset);
        this.motion.setValue(motion);
        this.perPosCount.setValue(perPosCount);

    }


    @Override
    public ParticleSpawnPattern copy() {
        return new AdvancedLineSpawnPattern(offset.value(), motion.value(), perPosCount.value());
    }

    @Override
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        return getParticleStart(position, particleNumber, additionalLocs, world, 0);
    }


    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world, int radialCount) {
        if (additionalLocs != null){
            Vector3d direction = additionalLocs.get(0);
            Vector3d data = additionalLocs.get(1);
            double perParticle = data.getY();
            AxisAngle4d axis = new AxisAngle4d(Math.PI * 2 * radialCount / perPosCount.value(), new org.joml.Vector3d(direction.getX(), direction.getY(), direction.getZ()));
            org.joml.Vector3d offset = axis.transform(new org.joml.Vector3d(1.0, 0.0, 0.0));
            Vector3d mcOffset = new Vector3d(offset.x, offset.y, offset.z);

            return new Tuple<>(position.add(direction.scale(perParticle * particleNumber)).add(mcOffset.scale(this.offset.value())), EMPTY_VEC);
        }
        return new Tuple<>(position, EMPTY_VEC);
    }

    public Vector3d getEndpoint(Vector3d position, @Nullable List<Vector3d> additionalLocs){
        if (additionalLocs != null && !additionalLocs.isEmpty()){
            return additionalLocs.get(0);
        }
        return position.add(new Vector3d(0.0, 5.0, 0.0));
    }

    protected Tuple<List<Vector3d>, Double> getSpawnData(Vector3d position, @Nullable List<Vector3d> additionalLocs){
        List<Vector3d> spawnData = new ArrayList<>();
        Vector3d endPoint = getEndpoint(position, additionalLocs);
        double distance = endPoint.distanceTo(position);
        Vector3d direction = endPoint.subtract(position).normalize();
        long particleCount = Math.round(distance * count.value());
        Vector3d lineData = new Vector3d(distance, distance / particleCount, 0);
        spawnData.add(direction);
        spawnData.add(lineData);
        return new Tuple<>(spawnData, distance);
    }

    @Override
    public void spawn(ParticleType<MKParticleData> particleType, Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs) {
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, additionalLocs);

        long particleCount = Math.round(spawnData.getB() * count.value());
        MKParticleData particleData = new MKParticleData(particleType, position, anim);
        for (int i = 0; i < particleCount; i++) {
            for (int c = 0; c < perPosCount.value(); c++){
                Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, spawnData.getA(), world, c);
                Vector3d pos = posAndMotion.getA();
                Vector3d mot = posAndMotion.getB();
                world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                        mot.getX(), mot.getY(), mot.getZ());
            }

        }


    }

    @Override
    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType, Vector3d offset, World world, ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs) {
        MKParticleData particleData = new MKParticleData(particleType, offset, anim, entity.getEntityId());
        Vector3d position = offset.add(entity.getPositionVec());
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, additionalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        for (int i = 0; i < particleCount; i++) {
            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, spawnData.getA(), world);
            Vector3d pos = posAndMotion.getA();
            Vector3d mot = posAndMotion.getB();
            world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                    mot.getX(), mot.getY(), mot.getZ());
        }
    }
}
