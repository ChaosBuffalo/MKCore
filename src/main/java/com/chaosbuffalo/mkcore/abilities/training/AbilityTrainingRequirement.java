package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbilityTrainingRequirement implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID, "ability_training_req.invalid");
    private static final String TYPE_ENTRY_NAME = "reqType";
    private final List<ISerializableAttribute<?>> attributes = new ArrayList<>();
    private final ResourceLocation typeName;

    public AbilityTrainingRequirement(ResourceLocation typeName) {
        this.typeName = typeName;
        setupAttributes();
    }

    protected void setupAttributes() {

    }

    public <D> AbilityTrainingRequirement(ResourceLocation typeName, Dynamic<D> dynamic) {
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

    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        Map<String, Dynamic<D>> map = dynamic.get("attributes").asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("attributes"),
                ops.createMap(attributes.stream().map(attr ->
                        Pair.of(ops.createString(attr.getName()), attr.serialize(ops))
                ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_OPTION);
    }
}
