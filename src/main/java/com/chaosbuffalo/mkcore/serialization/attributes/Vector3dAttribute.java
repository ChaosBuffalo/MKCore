package com.chaosbuffalo.mkcore.serialization.attributes;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.math.vector.Vector3d;

public class Vector3dAttribute extends SimpleAttribute<Vector3d> {


    public Vector3dAttribute(String name, Vector3d defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        Vector3d val = getValue();
        builder.put(ops.createString("x"), ops.createDouble(val.getX()));
        builder.put(ops.createString("y"), ops.createDouble(val.getY()));
        builder.put(ops.createString("z"), ops.createDouble(val.getZ()));
        return ops.createMap(builder.build());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        double x = dynamic.get("x").asDouble(0.0);
        double y = dynamic.get("y").asDouble(0.0);
        double z = dynamic.get("z").asDouble(0.0);
        setValue(new Vector3d(x, y, z));
    }

    @Override
    public void setValueFromString(String stringValue) {

    }

    @Override
    public boolean validateString(String stringValue) {
        return false;
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return false;
    }

    @Override
    public String valueAsString() {
        return String.format("%f,%f,%f", getValue().x, getValue().y, getValue().z);
    }
}
