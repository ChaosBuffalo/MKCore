package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.stats.CriticalStats;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.math.AxisAlignedBB;
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
}
