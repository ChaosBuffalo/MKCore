package com.chaosbuffalo.mkcore.effects;

import net.minecraft.nbt.CompoundNBT;

public abstract class ScalingValueEffectState extends MKEffectState {
    protected float base = 0.0f;
    protected float scale = 0.0f;
    protected float modScale = 1.0f;

    public void setScalingParameters(float base, float scale) {
        setScalingParameters(base, scale, 1.0f);
    }

    public void setScalingParameters(float base, float scale, float modScale) {
        this.base = base;
        this.scale = scale;
        this.modScale = modScale;
    }

    public float getScaledValue(int stacks) {
        return base + (scale * stacks);
    }

    public float getModifierScale() {
        return modScale;
    }

    @Override
    public void serializeStorage(CompoundNBT stateTag) {
        super.serializeStorage(stateTag);
        stateTag.putFloat("base", base);
        stateTag.putFloat("scale", scale);
        stateTag.putFloat("modScale", modScale);
    }

    @Override
    public void deserializeStorage(CompoundNBT stateTag) {
        super.deserializeStorage(stateTag);
        base = stateTag.getFloat("base");
        scale = stateTag.getFloat("scale");
        modScale = stateTag.getFloat("modScale");
    }
}
