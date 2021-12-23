package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;


public class EntityMKAreaEffectRenderer extends EntityRenderer<MKAreaEffectEntity> {
    public EntityMKAreaEffectRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull MKAreaEffectEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public boolean shouldRender(@Nonnull MKAreaEffectEntity entity, @Nonnull ClippingHelper clipping, double x, double y, double z) {
        return false;
    }
}
