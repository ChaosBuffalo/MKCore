package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.google.common.collect.Streams;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

@Mixin(PotionSpriteUploader.class)
public abstract class PotionSpriteUploaderMixins extends SpriteUploader {

    public PotionSpriteUploaderMixins(TextureManager textureManagerIn, ResourceLocation atlasTextureLocation, String prefixIn) {
        super(textureManagerIn, atlasTextureLocation, prefixIn);
    }


    /**
     * @author ralekdev
     * @reason Ensure MKEffect textures are baked into the texture atlas
     */
    @Overwrite
    protected Stream<ResourceLocation> getResourceLocations() {
        return Streams.concat(Registry.EFFECTS.keySet().stream(), MKCoreRegistry.EFFECTS.getKeys().stream());
    }

    /**
     * @author ralekdev
     * @reason Allow texture lookup for MKEffectInstance-based effects
     */
    @Overwrite
    public TextureAtlasSprite getSprite(Effect effectIn) {
        if (effectIn instanceof MKEffect.WrapperEffect) {
            MKEffect.WrapperEffect vanilla = (MKEffect.WrapperEffect) effectIn;

            ResourceLocation effectId = vanilla.getMKEffect().getId();
            return super.getSprite(effectId);
        }
        // Vanilla logic
        return super.getSprite(Registry.EFFECTS.getKey(effectIn));
    }
}
