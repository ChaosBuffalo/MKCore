package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class EntityLineEffectRenderer extends EntityRenderer<LineEffectEntity> {
    public EntityLineEffectRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(LineEffectEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }


    @Override
    public boolean shouldRender(@Nonnull LineEffectEntity entity, @Nonnull ClippingHelper clippingHelper, double x, double y, double z) {
        return false;
    }
}
