package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ParticleAnimationTrack implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    private static final String TYPE_NAME_FIELD = "trackType";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "particle_anim.invalid");

    private final ResourceLocation typeName;
    private final List<ISerializableAttribute<?>> attributes;
    private final AnimationTrackType trackType;
    protected int keyCount = 0;

    public enum AnimationTrackType {
        UNKNOWN,
        MOTION,
        SCALE,
        COLOR
    }

    public ParticleAnimationTrack(ResourceLocation typeName, AnimationTrackType trackType){
        this.typeName = typeName;
        this.attributes = new ArrayList<>();
        this.trackType = trackType;
    }

    public void apply(MKParticle particle) {

    }

    public abstract ParticleAnimationTrack copy();

    public ITextComponent getDescription(){
        return new TranslationTextComponent(String.format("%s.anim_track.%s.name",
                getTypeName().getNamespace(), getTypeName().getPath()));
    }

    public static ITextComponent getDescriptionFromType(ResourceLocation type){
        return new TranslationTextComponent(String.format("%s.anim_track.%s.name",
                type.getNamespace(), type.getPath()));
    }

    public AnimationTrackType getTrackType() {
        return trackType;
    }

    public void animate(MKParticle particle, float time, int trackTick, int duration, float partialTicks){

    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> attribute) {
        attributes.add(attribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
    }

    public void begin(MKParticle particle, int duration){

    }

    public void update(MKParticle particle, int tick, float time) {

    }

    public void end(MKParticle particle){

    }

    public float generateVariance(MKParticle particle){
        return (particle.getRand().nextFloat() * 2.0f) - 1.0f;
    }

    public Vector3d generateVarianceVector(MKParticle particle){
        return new Vector3d(generateVariance(particle), generateVariance(particle), generateVariance(particle));
    }


    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        deserializeAttributeMap(dynamic, "attributes");
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    @Override
    public ResourceLocation getTypeName() {
        return typeName;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }
}
