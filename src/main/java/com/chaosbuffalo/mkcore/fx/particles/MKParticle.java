package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class MKParticle extends SpriteTexturedParticle {
    private final boolean expireOnGround;
    private final Vector3d origin;
    protected ParticleKeyFrame currentFrame;
    private Consumer<MKParticle> onExpire;
    private final ParticleAnimation particleAnimation;
    private final Map<ParticleDataKey, Float> floatData;
    private final Map<ParticleDataKey, Vector3d> vector3dData;
    private final Map<ParticleDataKey, Vector3f> vector3fData;
    private final IParticleRenderType renderType;
    private float mkMinU;
    private float mkMinV;
    private float mkMaxU;
    private float mkMaxV;
    private int ticksSinceRender;
    @Nullable
    private final Entity source;
    private static final Vector3d EMPTY_VECTOR_3D = new Vector3d(0.0, 0.0, 0.0);
    private static final Vector3f EMPTY_VECTOR_3F = new Vector3f(0.0f, 0.0f, 0.0f);

    public static class ParticleDataKey {
        private final ParticleAnimationTrack animation;
        private final int index;

        public ParticleDataKey(ParticleAnimationTrack animation, int i){
            this.animation = animation;
            this.index = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParticleDataKey that = (ParticleDataKey) o;
            return index == that.index && animation.equals(that.animation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(animation, index);
        }
    }


    private MKParticle(ClientWorld world, double posX, double posY, double posZ,
                       float gravity,
                       float particleWidth, float particleHeight,
                       int maxAge, boolean expireOnGround, ParticleAnimation animation,
                       Vector3d origin, Entity source, IParticleRenderType renderType) {
        super(world, posX, posY, posZ);
        this.origin = origin;
        this.setSize(particleWidth, particleHeight);
        this.particleGravity = gravity;
        this.maxAge = maxAge;
        this.expireOnGround = expireOnGround;
        this.onExpire = null;
        this.age = 0;
        this.source = source;
        this.ticksSinceRender = 0;
        this.canCollide = false;
        this.currentFrame = new ParticleKeyFrame();
        this.particleAnimation = animation;
        this.floatData = new HashMap<>();
        this.vector3dData = new HashMap<>();
        this.vector3fData = new HashMap<>();
        this.maxAge = animation.getTickLength();
        this.renderType = renderType;
        animation.tick(this);
        animation.tickAnimation(this, 0.0f);
    }

    public void fixUV(){
        // there is not enough padding in between particles in the particle texture atlas, if we blur them
        // sometimes you'll get pixels from an adjacent particle, lets reduce our uvs by 10% to avoid this
        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();
        float diffU = (maxU - minU) * .1f;
        float diffV = (maxV - minV) * .1f;
        mkMinU = minU + diffU;
        mkMaxU = maxU - diffU;
        mkMinV = minV + diffV;
        mkMaxV = maxV - diffV;

    }

    public boolean hasSource(){
        return source != null;
    }


    public Optional<Entity> getSource() {
        return source != null ? Optional.of(source) : Optional.empty();
    }

    @Override
    protected float getMaxU() {
        return mkMaxU;
    }

    @Override
    protected float getMaxV() {
        return mkMaxV;
    }

    @Override
    protected float getMinU() {
        return mkMinU;
    }

    @Override
    protected float getMinV() {
        return mkMinV;
    }

    public Random getRand(){
        return rand;
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        particleAnimation.tickAnimation(this, partialTicks);
//        Vector3d particlePos = new Vector3d(posX, posY, posZ);
//        if (renderInfo.pos.squareDistanceTo(particlePos) < 1.0){
//            return;
//        }
        ticksSinceRender = 0;
        super.renderParticle(buffer, renderInfo, partialTicks);
    }

    public Vector3d getOrigin() {
        return getSource().map(ent -> origin.add(ent.getPositionVec())).orElse(origin);
    }

    public Vector3d getMotion(){
        return new Vector3d(motionX, motionY, motionZ);
    }

    public Vector3d getPosition(){
        return new Vector3d(posX, posY, posZ);
    }

    public Vector3d getInterpolatedPosition(float partialTicks){
        return new Vector3d(MathUtils.lerpDouble(prevPosX, posX, partialTicks),
                MathUtils.lerpDouble(prevPosY, posY, partialTicks),
                MathUtils.lerpDouble(prevPosZ, posZ, partialTicks));
    }

    public void setTrackFloatData(ParticleDataKey key, float value){
        floatData.put(key, value);
    }

    public float getTrackFloatData(ParticleDataKey key){
        return floatData.getOrDefault(key, 0.0f);
    }

    public void setTrackVector3dData(ParticleDataKey key, Vector3d vec){
        vector3dData.put(key, vec);
    }

    public Vector3d getTrackVector3dData(ParticleDataKey key){
        return vector3dData.getOrDefault(key, EMPTY_VECTOR_3D);
    }

    public void setTrackVector3fData(ParticleDataKey key, Vector3f vec){
        vector3fData.put(key, vec);
    }

    public Vector3f getTrackVector3fData(ParticleDataKey key){
        return vector3fData.getOrDefault(key, EMPTY_VECTOR_3F);
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
        return renderType;
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

    public float getParticleGravity(){
        return this.particleGravity;
    }


    public void tick() {
        particleAnimation.tick(this);
        ticksSinceRender++;
        if (ticksSinceRender > 1){
            particleAnimation.tickAnimation(this, 0.0f);
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.expire();
        if (!this.isExpired) {
//            this.motionY -= this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.onUpdate();
        }
    }

    public static class MKParticleFactory implements IParticleFactory<MKParticleData> {
        protected final IAnimatedSprite spriteSet;
        private final float gravity;
        private final float particleWidth;
        private final float particleHeight;
        private final int maxAge;
        private final boolean expireOnGround;
        private final IParticleRenderType renderType;
        private final Consumer<MKParticle> onExpire;

        public MKParticleFactory(IAnimatedSprite spriteSet,
                                 float gravity, float particleWidth, float particleHeight, int maxAge,
                                 boolean expireOnGround, IParticleRenderType renderType, Consumer<MKParticle> onExpire) {
            this.spriteSet = spriteSet;
            this.maxAge = maxAge;
            this.gravity = gravity;
            this.particleHeight = particleHeight;
            this.particleWidth = particleWidth;
            this.expireOnGround = expireOnGround;
            this.onExpire = onExpire;
            this.renderType = renderType;
        }

        public MKParticleFactory(IAnimatedSprite spriteSet,
                                 float gravity, float particleWidth, float particleHeight, int maxAge,
                                 boolean expireOnGround, Consumer<MKParticle> onExpire) {
            this(spriteSet, gravity, particleWidth, particleHeight, maxAge, expireOnGround,
                    ParticleRenderTypes.MAGIC_RENDERER, onExpire);
        }


        @Nullable
        @Override
        public Particle makeParticle(MKParticleData typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MKParticle particle = new MKParticle(worldIn, x, y, z,
                    gravity, particleWidth, particleHeight, maxAge, expireOnGround, typeIn.animation, typeIn.origin,
                    typeIn.hasSource() ? worldIn.getEntityByID(typeIn.getEntityId()) : null, renderType);
            particle.setMotion(xSpeed, ySpeed, zSpeed);
            particle.selectSpriteRandomly(this.spriteSet);
            particle.fixUV();
            particle.setOnExpire(onExpire);
            return particle;
        }
    }
}
