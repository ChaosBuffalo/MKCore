package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;


public class ParticleColorAnimationTrack extends ParticleAnimationTrack {
    protected float red;
    protected float green;
    protected float blue;
    protected float redVariance;
    protected float greenVariance;
    protected float blueVariance;
    private int keyCount = 0;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.color");
    private final MKParticle.ParticleDataKey COLOR = new MKParticle.ParticleDataKey(this, keyCount++);


    public ParticleColorAnimationTrack(float red, float green, float blue){
        this(red, green, blue, 0.0f, 0.0f, 0.0f);
    }

    public ParticleColorAnimationTrack(float red, float green, float blue,
                                       float redVariance, float greenVariance, float blueVariance){
        super(TYPE_NAME);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.redVariance = redVariance;
        this.greenVariance = greenVariance;
        this.blueVariance = blueVariance;
    }

    public ParticleColorAnimationTrack(){
        this(1.0f, 1.0f, 1.0f);
    }

    @Override
    public void begin(MKParticle particle) {
        particle.setTrackVector3fData(COLOR, new Vector3f(red + redVariance * generateVariance(particle),
                green + greenVariance * generateVariance(particle),
                blue + blueVariance * generateVariance(particle)));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        red = dynamic.get("red").asFloat(1.0f);
        green = dynamic.get("green").asFloat(1.0f);
        blue = dynamic.get("blue").asFloat(1.0f);
        redVariance = dynamic.get("redVariance").asFloat(0.0f);
        greenVariance = dynamic.get("greenVariance").asFloat(0.0f);
        blueVariance = dynamic.get("blueVariance").asFloat(0.0f);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        D sup = super.serialize(ops);
        builder.put(ops.createString("red"), ops.createFloat(red));
        builder.put(ops.createString("redVariance"), ops.createFloat(redVariance));
        builder.put(ops.createString("green"), ops.createFloat(green));
        builder.put(ops.createString("greenVariance"), ops.createFloat(greenVariance));
        builder.put(ops.createString("blue"), ops.createFloat(blue));
        builder.put(ops.createString("blueVariance"), ops.createFloat(blueVariance));
        return ops.mergeToMap(sup, builder.build()).result().orElse(sup);
    }

    public Vector3f getColor(MKParticle particle){
        return particle.getTrackVector3fData(COLOR);
    }

    @Override
    public void apply(MKParticle particle) {
        Vector3f color = getColor(particle);
        particle.setColor(color.getX(), color.getY(), color.getZ());
    }

    @Override
    public void animate(MKParticle particle, float time, int trackTick) {
        ParticleColorAnimationTrack current = particle.getCurrentFrame().getColorTrack();
        Vector3f from = current.getColor(particle);
        Vector3f to = getColor(particle);
        particle.setColor(
                MathUtils.lerp(from.getX(), to.getX(), time),
                MathUtils.lerp(from.getY(), to.getY(), time),
                MathUtils.lerp(from.getZ(), to.getZ(), time)
        );
    }

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setColorTrack(this);
    }
}
