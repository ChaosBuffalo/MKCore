package com.chaosbuffalo.mkcore.abilities.attributes;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class FloatAttribute extends AbilityAttribute<Float> {

    public FloatAttribute(String name, float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <T> T serialize(DynamicOps<T> ops) {
        return ops.createFloat(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asFloat(getDefaultValue()));
        MKCore.LOGGER.info("float dyn {} {} is {} def {}", dynamic, getName(), getValue(), getDefaultValue());
    }
}
