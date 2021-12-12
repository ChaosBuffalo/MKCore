package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public abstract class MCSkeleton {

    @Nullable
    public abstract MCBone getBone(String boneName);


}
