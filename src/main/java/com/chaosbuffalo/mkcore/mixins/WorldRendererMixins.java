package com.chaosbuffalo.mkcore.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixins {
    private CloudOption savedOption;

    @Shadow @Nullable private ShaderGroup field_239227_K_;

    @Shadow @Nullable private Framebuffer cloudFrameBuffer;

    @Shadow public abstract void renderClouds(MatrixStack matrixStackIn, float partialTicks, double viewEntityX, double viewEntityY, double viewEntityZ);

    // move clouds to before particle rendering
    @Inject(method= "updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V",
            at=@At(target="Lnet/minecraft/client/renderer/RenderTypeBuffers;getCrumblingBufferSource()Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;", value="INVOKE", ordinal = 2, shift = At.Shift.BY, by=2),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void proxyRedoClouds(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline,
                                 ActiveRenderInfo activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci){

        //draw clouds early before particles
        Vector3d vector3d = activeRenderInfoIn.getProjectedView();
        double d0 = vector3d.getX();
        double d1 = vector3d.getY();
        double d2 = vector3d.getZ();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
        Minecraft mc = Minecraft.getInstance();
        savedOption = mc.gameSettings.getCloudOption();
        if (this.field_239227_K_ != null){
            this.cloudFrameBuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
            RenderState.CLOUDS_TARGET.setupRenderState();
            this.renderClouds(matrixStackIn, partialTicks, d0, d1, d2);
            RenderState.CLOUDS_TARGET.clearRenderState();
        } else {
            this.renderClouds(matrixStackIn, partialTicks, d0, d1, d2);
        }
        RenderSystem.popMatrix();
        //pretend clouds are off for rest of render loop, disables vanilla cloud rendering since we just did it
        mc.gameSettings.cloudOption = CloudOption.OFF;

    }

    // restore cloud options to original settings for next render
    @Inject(method= "updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V",
            at=@At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void proxyEnd(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline,
                          ActiveRenderInfo activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci){
        Minecraft mc = Minecraft.getInstance();
        //set clouds back to previous setting
        mc.gameSettings.cloudOption = savedOption;
    }
}
