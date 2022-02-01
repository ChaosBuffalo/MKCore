package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
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

public class LineSpawnPattern extends ParticleSpawnPattern{
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.line");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 1.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final Vector3d EMPTY_VEC = new Vector3d(0.0, 0.0, 0.0);

    public LineSpawnPattern(){
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius);
    }

    public LineSpawnPattern(double xRadius, double yRadius, double zRadius){
        this();
        this.xRadius.setValue(xRadius);
        this.yRadius.setValue(yRadius);
        this.zRadius.setValue(zRadius);
    }


    @Override
    public ParticleSpawnPattern copy() {
        return new LineSpawnPattern(xRadius.value(), yRadius.value(), zRadius.value());
    }

    private Vector3d getRandomOffset(World world){
        double x = (2.0 * xRadius.value()) * world.getRandom().nextDouble() - xRadius.value();
        double y = (2.0 * yRadius.value()) * world.getRandom().nextDouble() - yRadius.value();
        double z = (2.0 * zRadius.value()) * world.getRandom().nextDouble() - zRadius.value();
        return new Vector3d(x, y, z);
    }

    @Override
    public void produceParticlesForIndex(Vector3d origin, int particleNumber, @Nullable List<Vector3d> additionalLocs,
                                         World world, Function<Vector3d, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        if (additionalLocs != null){
            Vector3d direction = additionalLocs.get(0);
            Vector3d data = additionalLocs.get(1);
            double perParticle = data.getY();
            Vector3d offset = getRandomOffset(world);
            Vector3d finalPos = origin.add(direction.scale(perParticle * particleNumber)).add(offset);
            finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(finalPos), finalPos, EMPTY_VEC));
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
}
