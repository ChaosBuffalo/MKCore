package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PillarSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.pillar");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 0.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.0);
    protected final IntAttribute layers = new IntAttribute("layers", 4);
    protected final DoubleAttribute layerHeight = new DoubleAttribute("layerHeight", 0.25);

    public PillarSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed, layers, layerHeight);
    }

    public PillarSpawnPattern(int count, Vector3d radii, double speed, int layers, double layerHeight){
        this();
        this.count.setValue(count);
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.layerHeight.setValue(layerHeight);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new PillarSpawnPattern(count.value(),
                new Vector3d(xRadius.value(), yRadius.value(), zRadius.value()),
                speed.value(), layers.value(), layerHeight.value());
    }

    @Override
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        int perLayer = count.value() / layers.value();
        int currentLayer = particleNumber / perLayer;
        int inLayer = particleNumber % perLayer;
        double height = currentLayer * layerHeight.value();
        double degrees = (360.0 / perLayer) * inLayer;
        Vector3d posVec = new Vector3d(position.x + xRadius.value() * Math.cos(Math.toRadians(degrees)),
                position.y + yRadius.value() + height, position.z + zRadius.value() * Math.sin(Math.toRadians(degrees)));
        Vector3d diffVec = posVec.subtract(position).normalize();
        return new Tuple<>(posVec, diffVec.scale(speed.value()));
    }

}