package com.chaosbuffalo.mkcore.fx.particles;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class IndicatorParticle extends SpriteTexturedParticle {
    private ActiveRenderInfo renderInfo;

    private IndicatorParticle(ClientWorld world, double posX, double posY, double posZ) {
        super(world, posX, posY, posZ);
        this.setSize(0.05f, 0.05f);
        this.particleGravity = 0.00F;
        this.maxAge = 1;
        this.renderInfo = null;
        this.particleScale = 1.0f;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return ParticleRenderTypes.ALWAYS_VISIBLE_RENDERER;
    }

    protected void expire() {
        if (this.maxAge-- <= 0) {
            this.setExpired();
        }
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        this.renderInfo = renderInfo;
        super.renderParticle(buffer, renderInfo, partialTicks);
    }

    @Override
    protected int getBrightnessForRender(float partialTick) {
        return LightTexture.packLight(15, 15);
    }

    @Override
    public float getScale(float partialTicks) {
        if (renderInfo != null){
            double dist = renderInfo.getProjectedView().distanceTo(new Vector3d(posX, posY, posZ));
            if (dist > 75){
                return (float) (dist / 75.0f) * particleScale;
            }
        }
        return particleScale;
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.expire();
    }

    public static class IndicatorFactory implements IParticleFactory<BasicParticleType> {
        protected final IAnimatedSprite spriteSet;

        public IndicatorFactory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            IndicatorParticle particle = new IndicatorParticle(worldIn, x, y, z);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }
}