package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.rendering.model.MKPlayerModel;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.core.player.PlayerAnimationModule;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Optional;


public class MKPlayerRenderer extends PlayerRenderer {
    private final BipedSkeleton<AbstractClientPlayerEntity, MKPlayerModel> skeleton;

    public MKPlayerRenderer(EntityRendererManager renderManager, boolean useSmallArms) {
        super(renderManager, useSmallArms);
        this.entityModel = new MKPlayerModel(0.0f, useSmallArms);
        this.skeleton = new BipedSkeleton<>((MKPlayerModel) entityModel);
    }

    @Override
    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        MKCore.getPlayer(entityIn).ifPresent(data -> {
            PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
            if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE){
                MKAbility ability = data.getAnimationModule().getCastingAbility();
                if (ability != null){
                    // do spell casting
                    if (ability.hasCastingParticles()){
                        ParticleAnimation anim = ParticleAnimationManager.getAnimation(ability.getCastingParticles());
                        if (anim != null){
                            Optional<Vector3d> leftPos = getHandPosition(partialTicks, entityIn, HandSide.LEFT);
                            leftPos.ifPresent(x -> anim.spawn(entityIn.getEntityWorld(), x, null));
                            Optional<Vector3d> rightPos = getHandPosition(partialTicks, entityIn, HandSide.RIGHT);
                            rightPos.ifPresent(x -> anim.spawn(entityIn.getEntityWorld(), x, null));
                        }
                    }
                }
            }
            data.getAnimationModule().getParticleInstances().forEach(instance -> {
                instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
            });
        });
    }

    @Override
    public void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn) {
        super.renderRightArm(matrixStackIn, bufferIn, combinedLightIn, playerIn);
        if (playerIn instanceof ClientPlayerEntity){
            MKCore.getPlayer(playerIn).ifPresent(data -> {
                PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
                if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE){
                    MKAbility ability = data.getAnimationModule().getCastingAbility();
                    if (ability != null){
                        // do spell casting
                        if (ability.hasCastingParticles()){
                            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                            if (anim != null){
                                Vector3d leftPos = getFirstPersonHandPosition(HandSide.LEFT,
                                        (ClientPlayerEntity) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getEntityWorld(), leftPos, null);
                                Vector3d rightPos = getFirstPersonHandPosition(HandSide.RIGHT,
                                        (ClientPlayerEntity) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getEntityWorld(), rightPos, null);
                            }
                        }
                    }
                }


                data.getAnimationModule().getParticleInstances().forEach(instance -> {
                    instance.update(playerIn, skeleton, 0.0f, getRenderOffset(playerIn, 0.0f));
                });
            });
        }

    }

    @Override
    public void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn) {
        super.renderLeftArm(matrixStackIn, bufferIn, combinedLightIn, playerIn);
        if (playerIn instanceof ClientPlayerEntity){
            MKCore.getPlayer(playerIn).ifPresent(data -> {
                PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
                if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE){
                    MKAbility ability = data.getAnimationModule().getCastingAbility();
                    if (ability != null){
                        // do spell casting
                        if (ability.hasCastingParticles()){
                            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                            if (anim != null){
                                Vector3d leftPos = getFirstPersonHandPosition(HandSide.LEFT,
                                        (ClientPlayerEntity) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getEntityWorld(), leftPos, null);
                                Vector3d rightPos = getFirstPersonHandPosition(HandSide.RIGHT,
                                        (ClientPlayerEntity) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getEntityWorld(), rightPos, null);
                            }
                        }
                    }
                }
                data.getAnimationModule().getParticleInstances().forEach(instance -> {
                    instance.update(playerIn, skeleton, 0.0f, getRenderOffset(playerIn, 0.0f));
                });
            });
        }
    }

    private Vector3d getOffsetSideFirstPerson(HandSide handIn, float equippedProg) {
        int i = handIn == HandSide.RIGHT ? 1 : -1;
        return new Vector3d(i * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
    }

    private Vector3d getFirstPersonHandPosition(HandSide handSide,
                                                ClientPlayerEntity playerEntityIn, float partialTicks,
                                                Vector3d renderOffset){
        double entX = MathHelper.lerp(partialTicks, playerEntityIn.prevPosX, playerEntityIn.getPosX());
        double entY = MathHelper.lerp(partialTicks, playerEntityIn.prevPosY, playerEntityIn.getPosY());
        double entZ = MathHelper.lerp(partialTicks, playerEntityIn.prevPosZ, playerEntityIn.getPosZ());
        float yaw = MathHelper.lerp(partialTicks, playerEntityIn.prevRenderYawOffset, playerEntityIn.renderYawOffset) * ((float)Math.PI / 180F);
        int handScalar = handSide == HandSide.RIGHT ? 1 : -1;
        // taken from first person render pathway
        Vector3d shoulderLoc = new Vector3d(handScalar * -0.4785682F, -0.094387F, 0.05731531F);
        // the rest from bone system
        MCBone shoulderBone = handSide == HandSide.RIGHT ? skeleton.rightArm : skeleton.leftArm;
        MCBone castLoc = handSide == HandSide.RIGHT ? skeleton.rightHand : skeleton.leftHand;
        Vector3d bonePos = MCBone.getOffsetForStopAt(castLoc, shoulderBone);
        bonePos = bonePos.add(shoulderLoc);
        //a height fudge factor
        return new Vector3d(entX, entY, entZ).add(renderOffset).add(new Vector3d(0.0, 1.25, 0.0)).add(bonePos.rotateYaw(-yaw));
    }


    private Optional<Vector3d> getHandPosition(float partialTicks, AbstractClientPlayerEntity entityIn, HandSide handSide){
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HandSide.LEFT ?
                        BipedSkeleton.LEFT_HAND_BONE_NAME : BipedSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
