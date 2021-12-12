package com.chaosbuffalo.mkcore.serialization.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class BooleanAttribute extends SimpleAttribute<Boolean> {

    public BooleanAttribute(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createBoolean(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asBoolean(getDefaultValue()));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Boolean.parseBoolean(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return "true".contains(stringValue) || "false".contains(stringValue);
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return !(string.equals("true") || string.equals("false"));
    }

    @Override
    public String valueAsString() {
        return Boolean.toString(getValue());
    }
}
