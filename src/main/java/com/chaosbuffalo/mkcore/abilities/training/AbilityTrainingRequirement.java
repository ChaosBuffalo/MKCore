package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbilityTrainingRequirement implements ISerializableAttributeContainer {
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "ability_training_req.invalid");
    private final List<ISerializableAttribute<?>> attributes = new ArrayList<>();
    private final ResourceLocation typeName;

    public AbilityTrainingRequirement(ResourceLocation typeName){
        this.typeName = typeName;
        setupAttributes();
    }

    protected void setupAttributes(){

    }

    public <D> AbilityTrainingRequirement(ResourceLocation typeName, Dynamic<D> dynamic){
        this(typeName);
        deserialize(dynamic);
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> iSerializableAttribute) {
        this.attributes.add(iSerializableAttribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... iSerializableAttributes) {
        this.attributes.addAll(Arrays.asList(iSerializableAttributes));
    }

    public abstract boolean check(MKPlayerData playerData, MKAbility ability);

    public abstract void onLearned(MKPlayerData playerData, MKAbility ability);

    public abstract IFormattableTextComponent describe(MKPlayerData playerData);

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("reqType"), ops.createString(getTypeName().toString()));
        builder.put(ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
        putAdditionalData(ops, builder);
        return ops.createMap(builder.build());
    }

    public <D> void readAdditionalData(Dynamic<D> dynamic) {

    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return new ResourceLocation(dynamic.get("reqType").asString().result().orElse(INVALID_OPTION.toString()));
    }

    public <D> void putAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {

    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        Map<String, Dynamic<D>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
        readAdditionalData(dynamic);
    }

//    AbilityTrainingRequirement NONE = new AbilityTrainingRequirement() {
//        @Override
//        public boolean check(MKPlayerData playerData, MKAbility ability) {
//            return false;
//        }
//
//        @Override
//        public void onLearned(MKPlayerData playerData, MKAbility ability) {
//
//        }
//
//        @Override
//        public ITextComponent describe(MKPlayerData playerData) {
//            return null;
//        }
//    };
}
