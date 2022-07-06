package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class BaseEffectEntityRenderer extends EntityRenderer<BaseEffectEntity> {
    public BaseEffectEntityRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(BaseEffectEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }


    @Override
    public boolean shouldRender(@Nonnull BaseEffectEntity entity, @Nonnull ClippingHelper clippingHelper, double x, double y, double z) {
        return true;
    }
}
