package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;


public class EntityUtils {

    private static final float DEFAULT_CRIT_RATE = 0.0f;
    private static final float DEFAULT_CRIT_DAMAGE = 1.0f;
    // Based on Skeleton Volume
    private static final double LARGE_VOLUME = 3.0 * .6 * .6 * 1.8;
    public static CriticalStats<Entity> ENTITY_CRIT = new CriticalStats<>(DEFAULT_CRIT_RATE, DEFAULT_CRIT_DAMAGE);

    public static void addCriticalStats(Class<? extends Entity> entityIn, int priority, float criticalChance,
                                        float damageMultiplier) {
        ENTITY_CRIT.addCriticalStats(entityIn, priority, criticalChance, damageMultiplier);
    }


    static {
        addCriticalStats(ArrowEntity.class, 0, .1f, 2.0f);
        addCriticalStats(SpectralArrowEntity.class, 1, .15f, 2.0f);
        addCriticalStats(ThrowableEntity.class, 0, .05f, 2.0f);
    }

    public static double calculateBoundingBoxVolume(LivingEntity entityIn) {
        AxisAlignedBB box = entityIn.getBoundingBox();
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    }

    public static boolean isLargeEntity(LivingEntity entityIn) {
        double vol = calculateBoundingBoxVolume(entityIn);
        return vol >= LARGE_VOLUME;
    }

    public static double getCooldownPeriod(LivingEntity entity) {
        return 1.0D / entity.getAttribute(Attributes.ATTACK_SPEED).getValue() *
                GameConstants.TICKS_PER_SECOND;
    }

    public static void shootArrow(LivingEntity source, AbstractArrowEntity arrowEntity, LivingEntity target, float launchVelocity){


        Vector3d targetVec = new Vector3d(target.getPosX(), target.getPosYHeight(0.6D), target.getPosZ());
        Vector3d diff = targetVec.subtract(arrowEntity.getPositionVec());
        Vector3d diffXZ = new Vector3d(diff.x, 0.0, diff.z);
        double groundDist = diffXZ.length();

        double vel = launchVelocity * GameConstants.TICKS_PER_SECOND;
        double seconds = groundDist / vel;
        double heightLostToGravity = arrowEntity.hasNoGravity() ? 0.0 : 0.05 * GameConstants.TICKS_PER_SECOND * seconds;

        double yDiff = diff.y;
        double yWithGravity = yDiff + heightLostToGravity;
        Vector3d targetPos = new Vector3d(diff.getX(), yWithGravity, diff.getZ());

        // emulates the logic skeleton uses to shoot an arrow
//        double d0 = target.getPosX() - source.getPosX();
//        double d1 = target.getPosYHeight(0.3333333333333333D) - arrowEntity.getPosY();
//        double d2 = target.getPosZ() - source.getPosZ();
//        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrowEntity.shoot(targetPos.getX(), targetPos.getY(), targetPos.getZ(), launchVelocity, (float)(
                14 - source.getEntityWorld().getDifficulty().getId() * 4));
    }

    public static boolean shootProjectileAtTarget(BaseProjectileEntity projectile, LivingEntity target,
                                                  float velocity, float accuracy) {

        ProjectileUtils.BallisticResult result = ProjectileUtils.solveBallisticArcStationaryTarget(
                projectile.getPositionVec(),
                target.getPositionVec().add(new Vector3d(0, target.getHeight() / 2.0, 0)),
                velocity, projectile.getGravityVelocity());

        if (!result.foundSolution) {
            MKCore.LOGGER.info("No solution found for {}", projectile.toString());
            return false;
        } else {
            projectile.shoot(result.lowArc.x, result.lowArc.y, result.lowArc.z, velocity, accuracy);
            return true;
        }
    }

    public static boolean canTeleportEntity(LivingEntity target){
        return !isLargeEntity(target);
    }

    public static void safeTeleportEntity(LivingEntity targetEntity, Vector3d teleLoc) {
        Entity finalTarget = targetEntity;
        if (targetEntity.isPassenger()){
            finalTarget = targetEntity.getLowestRidingEntity();
        }
        RayTraceResult colTrace = RayTraceUtils.rayTraceBlocks(finalTarget, finalTarget.getPositionVec(),
                teleLoc, false);
        if (colTrace.getType() == RayTraceResult.Type.BLOCK) {
            teleLoc = colTrace.getHitVec();
        }
        finalTarget.setPositionAndUpdate(teleLoc.x, teleLoc.y, teleLoc.z);
    }
}
