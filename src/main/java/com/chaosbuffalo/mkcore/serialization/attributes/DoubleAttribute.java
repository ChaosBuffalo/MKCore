package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.math.NumberUtils;

public class DoubleAttribute extends SimpleAttribute<Double> {

    public DoubleAttribute(String name, double defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createDouble(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asDouble(getDefaultValue()));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Double.parseDouble(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isNumeric(stringValue) || MathUtils.isNumeric(stringValue + "0");
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public String valueAsString() {
        return Double.toString(getValue());
    }
}
