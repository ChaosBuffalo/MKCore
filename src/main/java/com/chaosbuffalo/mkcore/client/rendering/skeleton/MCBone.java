package com.chaosbuffalo.mkcore.client.rendering.skeleton;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class MCBone {

    private final String boneName;
    private final MCBone parent;
    private final Vector3d boneLocation;

    public MCBone(String boneName, Vector3d boneLocation, @Nullable MCBone parent){
        this.boneName = boneName;
        this.parent = parent;
        this.boneLocation = boneLocation;
    }

    public boolean hasParent(){
        return this.parent != null;
    }

    @Nullable
    public MCBone getParent() {
        return parent;
    }

    public String getBoneName() {
        return boneName;
    }

    public Vector3d getBoneLocation(){
        return boneLocation;
    }

    public abstract float getPitch();

    public abstract float getYaw();

    public abstract float getRoll();

    public static Vector3d getOffsetForBone(MCBone bone){
        MCBone currentBone = bone;
        Vector3d finalLoc = bone.getBoneLocation();
        while (currentBone != null && currentBone.hasParent()){
            MCBone parent = currentBone.getParent();
            if (parent != null){
                finalLoc = finalLoc.rotatePitch(-parent.getPitch());
                finalLoc = finalLoc.rotateYaw(parent.getYaw());
                finalLoc = finalLoc.rotateRoll(parent.getRoll());
                finalLoc = finalLoc.add(parent.getBoneLocation());
            }
            currentBone = parent;
        }
        return finalLoc;
    }

    public static Optional<Vector3d> getPositionOfBoneInWorld(LivingEntity entityIn, MCSkeleton skeleton,
                                                              float partialTicks, Vector3d renderOffset, String boneName){
        MCBone bone = skeleton.getBone(boneName);
        if (bone != null){
            double entX = MathHelper.lerp(partialTicks, entityIn.prevPosX, entityIn.getPosX());
            double entY = MathHelper.lerp(partialTicks, entityIn.prevPosY, entityIn.getPosY());
            double entZ = MathHelper.lerp(partialTicks, entityIn.prevPosZ, entityIn.getPosZ());
            float yaw = MathHelper.lerp(partialTicks, entityIn.prevRenderYawOffset, entityIn.renderYawOffset) * ((float)Math.PI / 180F);

            //we need to handle swimming rots
            float swimTime = entityIn.getSwimAnimation(partialTicks);
            float pitch = 0.0f;
            Vector3d boneOffset = new Vector3d(0.0, 0.0, 0.0);


            Vector3d bonePos = MCBone.getOffsetForBone(bone);

            if (swimTime > 0.0f){
                float entPitch = entityIn.isInWater() ? -90.0F - entityIn.rotationPitch : -90.0F;
                float lerpSwim = MathHelper.lerp(swimTime, 0.0F, entPitch);
                pitch = ((float)Math.PI / 180F) * lerpSwim;
                if (entityIn.isActualySwimming()) {
                    boneOffset = new Vector3d(0.0, -1.0, -0.3);
                }
                bonePos = bonePos.add(boneOffset);
                bonePos = bonePos.rotatePitch(pitch);
//                bonePos = bonePos.add(boneOffset);
            }

            return Optional.of(new Vector3d(entX, entY, entZ).add(renderOffset).add(bonePos.rotateYaw(-yaw)));
        } else {
            return Optional.empty();
        }


    }

}
