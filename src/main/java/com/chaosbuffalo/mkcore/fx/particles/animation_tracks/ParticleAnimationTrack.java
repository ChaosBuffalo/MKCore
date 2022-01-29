package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
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

public abstract class ParticleAnimationTrack implements ISerializableAttributeContainer {
    private final ResourceLocation typeName;
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "particle_anim.invalid");
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

    public void end(MKParticle particle){

    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public void begin(MKParticle particle, int duration){

    }

    public <D> D serialize(DynamicOps<D> ops){
        return ops.createMap(ImmutableMap.of(
                ops.createString("trackType"), ops.createString(getTypeName().toString()),
                ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))
        ));
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("trackType").asString().result().orElse(INVALID_OPTION.toString()));
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        Map<String, Dynamic<D>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    public float generateVariance(MKParticle particle){
        return (particle.getRand().nextFloat() * 2.0f) - 1.0f;
    }

    public Vector3d generateVarianceVector(MKParticle particle){
        return new Vector3d(generateVariance(particle), generateVariance(particle), generateVariance(particle));
    }

    public void update(MKParticle particle, int tick, float time) {

    }
}
