package com.chaosbuffalo.mkcore.abilities.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class FloatAttribute extends AbilityAttribute<Float> {

    public FloatAttribute(String name, float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createFloat(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asFloat(getDefaultValue()));
    }
}
