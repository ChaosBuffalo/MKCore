package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class ModelRendererBone extends MCBone {
    private final ModelRenderer modelRenderer;
    private boolean invertY;
    private boolean invertX;
    private boolean invertZ;

    public ModelRendererBone(String boneName, ModelRenderer modelRenderer, @Nullable MCBone parent, boolean invertY, boolean invertX, boolean invertZ) {
        super(boneName, new Vector3d(modelRenderer.rotationPointX / 16.0,
                modelRenderer.rotationPointY / 16.0, modelRenderer.rotationPointZ / 16.0), parent);
        this.modelRenderer = modelRenderer;
        this.invertY = invertY;
        this.invertX = invertX;
        this.invertZ = invertZ;

    }

    public ModelRenderer getModelRenderer() {
        return modelRenderer;
    }

    @Override
    public Vector3d getBoneLocation() {

        return new Vector3d(
                (invertX ? -1.0 : 1.0) * modelRenderer.rotationPointX / 16.0,
                (invertY ? -1.0 : 1.0) * modelRenderer.rotationPointY / 16.0,
                (invertZ ? -1.0 : 1.0) * modelRenderer.rotationPointZ / 16.0
        );
//        return invertY ? new Vector3d(-modelRenderer.rotationPointX / 16.0, -modelRenderer.rotationPointY / 16.0, -modelRenderer.rotationPointZ / 16.0)
//                : new Vector3d(modelRenderer.rotationPointX / 16.0, modelRenderer.rotationPointY / 16.0, modelRenderer.rotationPointZ / 16.0) ;
    }

    @Override
    public float getPitch(){
        return modelRenderer.rotateAngleX;
    }

    @Override
    public float getYaw(){
        return modelRenderer.rotateAngleY;
    }

    @Override
    public float getRoll(){
        return modelRenderer.rotateAngleZ;
    }
}
