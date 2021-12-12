package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.math.NumberUtils;

public class IntAttribute extends SimpleAttribute<Integer> {

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

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Integer.parseInt(stringValue));
    }

    @Override
    public String valueAsString() {
        return Integer.toString(getValue());
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isInteger(stringValue) || MathUtils.isInteger(stringValue +  "0");
    }
}
