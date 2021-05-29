package com.chaosbuffalo.mkcore.abilities.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class IntAttribute extends AbilityAttribute<Integer> {

    public IntAttribute(String name, int defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createInt(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asInt(getDefaultValue()));
    }
}
