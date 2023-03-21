package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class ParticleRenderTypes {

    public static final ParticleRenderType MAGIC_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.001F);
            RenderSystem.disableLighting();
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
            RenderSystem.disableBlend();
//            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":magic_render_type";
        }
    };


    public static final ParticleRenderType BLACK_MAGIC_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.001F);
            RenderSystem.disableLighting();
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
            RenderSystem.disableBlend();
//            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":magic_render_type";
        }
    };



    public static final ParticleRenderType ALWAYS_VISIBLE_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
//            RenderSystem.enableDepthTest();
//            RenderSystem.enableBlend();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
//            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.001F);
            RenderSystem.disableLighting();
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
//            textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).setBlurMipmap(true, false);
            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

//            Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).restoreLastBlurMipmap();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
//            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":always_visible";
        }
    };
}

