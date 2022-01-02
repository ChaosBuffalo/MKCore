package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ParticleSpawnPattern implements ISerializableAttributeContainer {
    protected final List<ISerializableAttribute<?>> attributes;
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.invalid");
    private final ResourceLocation type;
    protected final IntAttribute count = new IntAttribute("count", 10);


    public ParticleSpawnPattern(ResourceLocation type){
        this.type = type;
        this.attributes = new ArrayList<>();
        addAttributes(count);
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("type"), ops.createString(type.toString()));
        builder.put(ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
        return ops.createMap(builder.build());
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

    public abstract ParticleSpawnPattern copy();

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("type").asString().result().orElse(INVALID_OPTION.toString()));
    }

    public ITextComponent getDescription(){
        return new TranslationTextComponent(String.format("%s.spawn_pattern.%s.name", type.getNamespace(), type.getPath()));
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

    public abstract Tuple<Vector3d, Vector3d> getParticleStart(Vector3d position, int particleNumber, @Nullable List<Vector3d> additionalLocs, World world);

    public void spawn(ParticleType<MKParticleData> particleType,
                      Vector3d position, World world, ParticleAnimation anim, @Nullable List<Vector3d> additionalLocs){
        MKParticleData particleData = new MKParticleData(particleType, position, anim);
        for (int i = 0; i < count.value(); i++) {
            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, additionalLocs, world);
            Vector3d pos = posAndMotion.getA();
            Vector3d mot = posAndMotion.getB();
            world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                    mot.getX(), mot.getY(), mot.getZ());
        }
    }

    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
                                      Vector3d offset, World world,
                                      ParticleAnimation anim, Entity entity, List<Vector3d> additionalLocs){
        MKParticleData particleData = new MKParticleData(particleType, offset, anim, entity.getEntityId());
        Vector3d position = offset.add(entity.getPositionVec());
        for (int i = 0; i < count.value(); i++) {
            Tuple<Vector3d, Vector3d> posAndMotion = getParticleStart(position, i, additionalLocs, world);
            Vector3d pos = posAndMotion.getA();
            Vector3d mot = posAndMotion.getB();
            world.addOptionalParticle(particleData, true, pos.getX(), pos.getY(), pos.getZ(),
                    mot.getX(), mot.getY(), mot.getZ());
        }

    }
}
