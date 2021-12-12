package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.math.NumberUtils;

public class FloatAttribute extends SimpleAttribute<Float> {

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

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Float.parseFloat(stringValue));
    }

    @Override
    public String valueAsString() {
        return Float.toString(getValue());
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isNumeric(stringValue) || MathUtils.isNumeric(stringValue + "0");
    }
}
