package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;

public class ParticleRenderScaleAnimationTrack extends ParticleAnimationTrack {
    private float renderScale;
    private float maxVariance;
    private int keyCount = 0;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.render_scale");
    private final MKParticle.ParticleDataKey VARIANCE_KEY = new MKParticle.ParticleDataKey(this, keyCount++);


    public ParticleRenderScaleAnimationTrack(float scale, float maxVariance){
        super(TYPE_NAME);
        this.renderScale = scale;
        this.maxVariance = maxVariance;
    }

    public ParticleRenderScaleAnimationTrack(){
        this(1.0f, 0.0f);
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setScale(getScaleWithVariance(particle));
    }

    protected float getScaleWithVariance(MKParticle particle){
        return renderScale + (particle.getTrackFloatData(VARIANCE_KEY) * maxVariance);
    }

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setScaleTrack(this);
    }

    @Override
    public void begin(MKParticle particle) {
        particle.setTrackFloatData(VARIANCE_KEY, generateVariance(particle));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        renderScale = dynamic.get("renderScale").asFloat(1.0f);
        maxVariance = dynamic.get("maxVariance").asFloat(0.0f);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        D sup = super.serialize(ops);
        builder.put(ops.createString("renderScale"), ops.createFloat(renderScale));
        builder.put(ops.createString("maxVariance"), ops.createFloat(maxVariance));
        return ops.mergeToMap(sup, builder.build()).result().orElse(sup);
    }

    @Override
    public void animate(MKParticle particle, float time, int trackTick) {
        particle.setScale(MathUtils.lerp(particle.getCurrentFrame().getScaleTrack().getScaleWithVariance(particle),
                getScaleWithVariance(particle), time));
    }
}
