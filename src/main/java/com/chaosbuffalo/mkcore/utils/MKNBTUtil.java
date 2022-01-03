package com.chaosbuffalo.mkcore.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class MKNBTUtil {

    public static void writeResourceLocation(CompoundNBT tag, String name, ResourceLocation value) {
        tag.putString(name, value.toString());
    }

    public static ResourceLocation readResourceLocation(CompoundNBT tag, String name) {
        String raw = tag.getString(name);
        return new ResourceLocation(raw);
    }

    public static Vector3d readVector3(CompoundNBT nbt, String name) {
        CompoundNBT tag = nbt.getCompound(name);
        return new Vector3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }

    public static void writeVector3d(CompoundNBT tag, String name, Vector3d value) {
        CompoundNBT state = new CompoundNBT();
        state.putDouble("X", value.getX());
        state.putDouble("Y", value.getY());
        state.putDouble("Z", value.getZ());
        tag.put(name, state);
    }
}
