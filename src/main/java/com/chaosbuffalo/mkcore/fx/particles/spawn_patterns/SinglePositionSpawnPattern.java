package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SinglePositionSpawnPattern extends ParticleSpawnPattern{
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.single");

    public SinglePositionSpawnPattern() {
        super(TYPE);
        count.setValue(1);
    }

    @Override
    public SinglePositionSpawnPattern copy() {
        return new SinglePositionSpawnPattern();
    }

    @Override
    public void produceParticlesForIndex(Vector3d origin, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world, Function<Vector3d, MKParticleData> particleDataSupplier, List<ParticleSpawnEntry> finalParticles) {
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), origin, new Vector3d(0, 0, 0)));
    }
}
