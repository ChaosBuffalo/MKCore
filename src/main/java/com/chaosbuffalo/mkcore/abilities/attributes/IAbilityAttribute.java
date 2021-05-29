package com.chaosbuffalo.mkcore.abilities.attributes;


import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public interface IAbilityAttribute<T> {
    T getValue();

    void setValue(T newValue);

    String getName();

    <D> D serialize(DynamicOps<D> ops);

    <D> void deserialize(Dynamic<D> dynamic);
}
