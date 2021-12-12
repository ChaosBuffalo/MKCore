package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

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
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        return new Tuple<>(position, new Vector3d(0, 0, 0));
    }
}
