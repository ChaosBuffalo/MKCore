package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ParticleEffectInstance implements ISerializableAttributeContainer {
    protected final List<ISerializableAttribute<?>> attributes;
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID,
            "particle_effect_instance.invalid");
    protected final ResourceLocationAttribute particleAnimName = new ResourceLocationAttribute("particleAnimName",
            INVALID_OPTION);
    private UUID instanceUUID;
    private ParticleAnimation animation;
    private final ResourceLocation instanceType;

    public ParticleEffectInstance(ResourceLocation instanceType){
        this(instanceType, UUID.randomUUID());
    }

    public ParticleEffectInstance(ResourceLocation instanceType, UUID instanceUUID){
        this.attributes = new ArrayList<>();
        addAttribute(particleAnimName);
        this.instanceType = instanceType;
        this.instanceUUID = instanceUUID;
    }


    public void setInstanceUUID(UUID instanceUUID) {
        this.instanceUUID = instanceUUID;
    }

    public abstract void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vector3d offset);

    public ResourceLocation getParticleAnimName() {
        return particleAnimName.getValue();
    }

    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    public Optional<ParticleAnimation> getAnimation(){
        if (animation == null){
            this.animation = ParticleAnimationManager.getAnimation(getParticleAnimName());
        }
        if (animation != null){
            return Optional.of(animation);
        } else {
            return Optional.empty();
        }
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

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("type"), ops.createString(instanceType.toString()));
        builder.put(ops.createString("instanceUUID"), ops.createString(instanceUUID.toString()));
        builder.put(ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        this.instanceUUID = UUID.fromString(dynamic.get("instanceUUID").asString(UUID.randomUUID().toString()));
        Map<String, Dynamic<D>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("type").asString().result().orElse(INVALID_OPTION.toString()));
    }

}
