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
        return new LineSpawnPattern(xRadius.getValue(), yRadius.getValue(), zRadius.getValue());
    }

    private Vector3d getRandomOffset(World world){
        double x = (2.0 * xRadius.getValue()) * world.getRandom().nextDouble() - xRadius.getValue();
        double y = (2.0 * yRadius.getValue()) * world.getRandom().nextDouble() - yRadius.getValue();
        double z = (2.0 * zRadius.getValue()) * world.getRandom().nextDouble() - zRadius.getValue();
        return new Vector3d(x, y, z);
    }

    @Override
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        if (additionalLocs != null){
            Vector3d direction = additionalLocs.get(0);
            Vector3d data = additionalLocs.get(1);
            double perParticle = data.getY();
            Vector3d offset = getRandomOffset(world);
            return new Tuple<>(position.add(direction.scale(perParticle * particleNumber)).add(offset), EMPTY_VEC);
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
        long particleCount = Math.round(distance * count.getValue());
        Vector3d lineData = new Vector3d(distance, distance / particleCount, 0);
        spawnData.add(direction);
        spawnData.add(lineData);
        return new Tuple<>(spawnData, distance);
    }

    @Override
    public void spawn(ParticleType<MKParticleData> particleType, Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs) {
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, additionalLocs);

        long particleCount = Math.round(spawnData.getB() * count.getValue());
        MKParticleData particleData = new MKParticleData(particleType, position, anim);
        for (int i = 0; i < particleCount; i++) {
            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, spawnData.getA(), world);
            Vector3d pos = posAndMotion.getA();
            Vector3d mot = posAndMotion.getB();
            world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                    mot.getX(), mot.getY(), mot.getZ());
        }


    }

    @Override
    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType, Vector3d offset, World world, ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs) {
        MKParticleData particleData = new MKParticleData(particleType, offset, anim, entity.getEntityId());
        Vector3d position = offset.add(entity.getPositionVec());
        Tuple<List<Vector3d>, Double> spawnData = getSpawnData(position, additionalLocs);
        long particleCount = Math.round(spawnData.getB() * count.getValue());
        for (int i = 0; i < particleCount; i++) {
            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, spawnData.getA(), world);
            Vector3d pos = posAndMotion.getA();
            Vector3d mot = posAndMotion.getB();
            world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                    mot.getX(), mot.getY(), mot.getZ());
        }
    }
}
