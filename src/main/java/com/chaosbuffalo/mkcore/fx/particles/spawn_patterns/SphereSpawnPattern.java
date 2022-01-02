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

public class SphereSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.sphere");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 1.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.00);
    protected final IntAttribute layers = new IntAttribute("layers", 4);

    public SphereSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed, layers);
        this.count.setValue(40);
    }

    public SphereSpawnPattern(int count, Vector3d radii, double speed, int layers){
        this();
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.count.setValue(count);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new SphereSpawnPattern(count.value(),
                new Vector3d(xRadius.value(), yRadius.value(), zRadius.value()),
                speed.value(),
                layers.value());
    }

    @Override
    public Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world) {
        int perLayer = count.value() / layers.value();
        particleNumber = particleNumber % (perLayer * layers.value());
        int currentLayer = particleNumber / perLayer;
        int realNum = particleNumber % perLayer;
        double ratio = (double) (currentLayer + 1) / (double) (layers.value() + 2);
        double scaledRatio = 2.0 * (ratio - 0.5);
        double realDegrees = (360.0 / perLayer) * realNum;
        double inverseScale = 1.0 - Math.pow(scaledRatio, 2.0);
        Vector3d posVec = new Vector3d(
                position.x + (xRadius.value() * inverseScale * Math.cos(Math.toRadians(realDegrees))),
                scaledRatio * yRadius.value() + position.y,
                position.z + (zRadius.value() * inverseScale * Math.sin(Math.toRadians(realDegrees))));
        Vector3d diffVec = posVec.subtract(position).normalize();
        return new Tuple<>(posVec, diffVec.scale(speed.value()));
    }
}
