package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.math.AxisAngle;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdvancedLineSpawnPattern extends ParticleSpawnPattern{
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.advanced_line");
    protected final DoubleAttribute offset = new DoubleAttribute("offset", 0.0);
    protected final DoubleAttribute motion = new DoubleAttribute("motion", 0.0);
    protected final IntAttribute perPosCount = new IntAttribute("per_pos_count", 10);

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
    public void produceParticlesForIndex(Vector3d origin, int particleNumber, @Nullable List<Vector3d> additionalLocs,
                                         World world, Function<Vector3d, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {

        if (additionalLocs != null) {
            Vector3d direction = additionalLocs.get(0);
            Vector3d data = additionalLocs.get(1);
            for (int i = 0; i < perPosCount.value(); i++) {
                AxisAngle axis = new AxisAngle(Math.PI * 2 * i / perPosCount.value(), direction.getX(), direction.getY(), direction.getZ());
                Vector3d mcOffset = axis.transform(new Vector3d(1.0, 0.0, 0.0));
                double perParticle = data.getY();
                Vector3d finalOrigin = origin.add(direction.scale(perParticle * particleNumber));
                Vector3d spawnPos = finalOrigin.add(mcOffset.scale(this.offset.value()));
                Vector3d motion = finalOrigin.subtract(spawnPos).normalize().scale(this.motion.value());
                finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(finalOrigin), spawnPos, motion));
            }
        }
    }

    @Override
    public void spawn(ParticleType<MKParticleData> particleType,
                      Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs){
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, additionalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, pos, anim),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles){
            spawnParticle(world, entry);
        }
    }

    @Override
    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
                                      Vector3d offset, World world,
                                      ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs){
        Vector3d position = offset.add(entity.getPositionVec());
        List<Vector3d> finalLocs = additionalLocs.stream().map(
                x -> x.add(entity.getPositionVec())).collect(Collectors.toList());
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, finalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, offset, anim, entity.getEntityId()),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles){
            spawnParticle(world, entry);
        }
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
}
