package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleColorAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.ColorFloatAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;


public class ParticleLerpColorAnimationTrack extends ParticleColorAnimationTrack {
    protected final ColorFloatAttribute red = new ColorFloatAttribute("red", 1.0f);
    protected final ColorFloatAttribute green = new ColorFloatAttribute("green", 1.0f);
    protected final ColorFloatAttribute blue = new ColorFloatAttribute("blue", 1.0f);
    protected final ColorFloatAttribute redVariance = new ColorFloatAttribute("redVariance", 0.0f);
    protected final ColorFloatAttribute greenVariance = new ColorFloatAttribute("greenVariance", 0.0f);
    protected final ColorFloatAttribute blueVariance = new ColorFloatAttribute("blueVariance", 0.0f);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.lerp_color");
    private final MKParticle.ParticleDataKey COLOR = new MKParticle.ParticleDataKey(this, keyCount++);


    public ParticleLerpColorAnimationTrack(float red, float green, float blue){
        this();
        this.red.setValue(red);
        this.green.setValue(green);
        this.blue.setValue(blue);
    }

    public ParticleLerpColorAnimationTrack(float red, float green, float blue,
                                           float redVariance, float greenVariance, float blueVariance){
        this(red, green, blue);
        this.redVariance.setValue(redVariance);
        this.greenVariance.setValue(greenVariance);
        this.blueVariance.setValue(blueVariance);
    }

    @Override
    public ParticleLerpColorAnimationTrack copy() {
        return new ParticleLerpColorAnimationTrack(red.getValue(), green.getValue(), blue.getValue(),
                redVariance.getValue(), greenVariance.getValue(), blueVariance.getValue());
    }

    public ParticleLerpColorAnimationTrack(){
        super(TYPE_NAME);
        addAttributes(red, green, blue, redVariance, greenVariance, blueVariance);
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        particle.setTrackVector3fData(COLOR, new Vector3f(
                getColorWithVariance(red.getValue(), redVariance.getValue(), generateVariance(particle)),
                getColorWithVariance(green.getValue(), greenVariance.getValue(), generateVariance(particle)),
                getColorWithVariance(blue.getValue(), blueVariance.getValue(), generateVariance(particle))));
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
    public void animate(MKParticle particle, float time, int trackTick, int duration, float partialTicks) {
        ParticleColorAnimationTrack current = particle.getCurrentFrame().getColorTrack();
        Vector3f from = current.getColor(particle);
        Vector3f to = getColor(particle);
        particle.setColor(
                MathUtils.lerp(from.getX(), to.getX(), time),
                MathUtils.lerp(from.getY(), to.getY(), time),
                MathUtils.lerp(from.getZ(), to.getZ(), time)
        );
    }
}
