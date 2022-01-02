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

public class SpiralSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.spiral");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 0.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.0);
    protected final IntAttribute fullRotations = new IntAttribute("rots", 1);
    protected final IntAttribute layers = new IntAttribute("layers", 4);
    protected final DoubleAttribute layerHeight = new DoubleAttribute("layerHeight", 0.25);

    public SpiralSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed, layers, layerHeight, fullRotations);
    }

    public SpiralSpawnPattern(int count, Vector3d radii, double speed, int layers, double layerHeight, int fullRotations){
        this();
        this.count.setValue(count);
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.layerHeight.setValue(layerHeight);
        this.fullRotations.setValue(fullRotations);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new SpiralSpawnPattern(count.value(),
                new Vector3d(xRadius.getValue(), yRadius.getValue(), zRadius.getValue()),
                speed.getValue(), layers.value(), layerHeight.getValue(), fullRotations.value());
    }

    @Override
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        int perLayer = count.value() / layers.value();
        int currentLayer = particleNumber / perLayer;
        int inLayer = particleNumber % perLayer;
        int rots = fullRotations.value();
        if (rots == 0){
            rots = 1;
        }
        int inRot = count.value() / rots;
        float heightRatio = (float)(inLayer) / perLayer;
        double height = currentLayer * layerHeight.getValue() + (heightRatio * layerHeight.getValue());
        double degrees = (360.0 / inRot) * particleNumber;
        Vector3d posVec = new Vector3d(position.x + xRadius.getValue() * Math.cos(Math.toRadians(degrees)),
                position.y + yRadius.getValue() + height, position.z + zRadius.getValue() * Math.sin(Math.toRadians(degrees)));
        Vector3d diffVec = posVec.subtract(position).normalize();
        return new Tuple<>(posVec, diffVec.scale(speed.getValue()));
    }
}