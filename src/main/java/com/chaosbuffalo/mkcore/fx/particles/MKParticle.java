package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.visual_attributes.IParticleAttribute;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class MKParticle extends SpriteTexturedParticle {
    private final boolean hasFriction;
    private final boolean expireOnGround;
    protected ParticleKeyFrame currentFrame;
    private Consumer<MKParticle> onExpire;
    private final ParticleAnimation particleAnimation;
    private final Map<IParticleAttribute, Float> varianceMap;


    private MKParticle(ClientWorld world, double posX, double posY, double posZ,
                       boolean hasFriction, float gravity,
                       float particleWidth, float particleHeight,
                       int maxAge, boolean expireOnGround, ParticleAnimation animation) {
        super(world, posX, posY, posZ);
        this.setSize(particleWidth, particleHeight);
        this.particleGravity = gravity;
        this.hasFriction = hasFriction;
        this.maxAge = maxAge;
        this.expireOnGround = expireOnGround;
        this.onExpire = null;
        this.age = 0;
        this.currentFrame = new ParticleKeyFrame();
        this.particleAnimation = animation;
        this.varianceMap = new HashMap<>();
        animation.tickAnimation(this, 0.0f);
    }



    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        particleAnimation.tickAnimation(this, partialTicks);
        Vector3d particlePos = new Vector3d(posX, posY, posZ);
        if (renderInfo.pos.squareDistanceTo(particlePos) < 1.0){
            return;
        }
        super.renderParticle(buffer, renderInfo, partialTicks);
    }

    public float getVarianceForAttribute(IParticleAttribute attribute){
        return varianceMap.getOrDefault(attribute, 0.0f);
    }

    public void pushVariance(IParticleAttribute attribute){
        varianceMap.put(attribute, attribute.generateVariance(this, rand));
    }

    public ParticleKeyFrame getCurrentFrame() {
        return currentFrame;
    }

    public void setScale(float scale){
        this.particleScale = scale;
    }

    public int getAge(){
        return age;
    }

    @Override
    protected int getBrightnessForRender(float partialTick) {
        return LightTexture.packLight(15, 15);
    }

    public void setOnExpire(Consumer<MKParticle> onExpire) {
        this.onExpire = onExpire;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return ParticleRenderTypes.MAGIC_RENDERER;
    }

    protected void expire() {
        if (this.age++ >= maxAge) {
            if (onExpire != null){
                onExpire.accept(this);
            }
            this.setExpired();
        }
    }

    protected void onUpdate(){
        if (this.onGround && expireOnGround){
            this.setExpired();
        }
    }

    public void setMotion(double x, double y, double z){
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    public double getMotionX(){
        return motionX;
    }

    public double getMotionY(){
        return motionY;
    }

    public double getMotionZ(){
        return motionZ;
    }


    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.expire();
        if (!this.isExpired) {
            this.motionY -= this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.onUpdate();
            if (!this.isExpired && hasFriction) {
                this.motionX *= 0.98F;
                this.motionY *= 0.98F;
                this.motionZ *= 0.98F;
            }
        }
    }

    public static class MKParticleFactory implements IParticleFactory<BasicParticleType> {
        protected final IAnimatedSprite spriteSet;
        private final boolean hasFriction;
        private final float gravity;
        private final float particleWidth;
        private final float particleHeight;
        private final int maxAge;
        private final boolean expireOnGround;
        private final ParticleAnimation animation;
        private Consumer<MKParticle> onExpire;

        public MKParticleFactory(IAnimatedSprite spriteSet, boolean hasFriction,
                                 float gravity, float particleWidth, float particleHeight, int maxAge,
                                 boolean expireOnGround, Consumer<MKParticle> onExpire, ParticleAnimation animation) {
            this.spriteSet = spriteSet;
            this.hasFriction = hasFriction;
            this.maxAge = maxAge;
            this.gravity = gravity;
            this.particleHeight = particleHeight;
            this.particleWidth = particleWidth;
            this.expireOnGround = expireOnGround;
            this.onExpire = onExpire;
            this.animation = animation;
        }

        @Nullable
        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed) {
            MKParticle particle = new MKParticle(worldIn, x, y, z, hasFriction,
                    gravity, particleWidth, particleHeight, maxAge, expireOnGround, animation);
            particle.setMotion(xSpeed, ySpeed, zSpeed);
            particle.selectSpriteRandomly(this.spriteSet);
            particle.setOnExpire(onExpire);
            return particle;
        }


    }
}
