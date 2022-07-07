package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class TargetUtil {

    private static final Predicate<Entity> defaultFilter = e -> EntityPredicates.IS_ALIVE.test(e) && EntityPredicates.NOT_SPECTATING.test(e);

    public static <E extends Entity> List<E> getEntitiesInLine(Class<E> clazz, final Entity mainEntity,
                                                               Vector3d from, Vector3d to, Vector3d expansion,
                                                               float growth, final Predicate<E> filter) {
        Predicate<E> predicate = e -> defaultFilter.test(e) && filter.test(e);
        AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(from), new BlockPos(to))
                .expand(expansion.x, expansion.y, expansion.z)
                .grow(growth);
        return mainEntity.getEntityWorld().getEntitiesWithinAABB(clazz, bb, predicate);
    }

    public static LivingEntity getSingleLivingTarget(LivingEntity caster, float distance,
                                                     BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        return getSingleLivingTarget(LivingEntity.class, caster, distance, validTargetChecker);
    }

    public static class LivingOrPosition {
        @Nullable
        private final LivingEntity entity;
        @Nullable
        private final Vector3d position;

        public LivingOrPosition(Vector3d loc) {
            position = loc;
            entity = null;
        }

        public LivingOrPosition(LivingEntity entity) {
            this.entity = entity;
            this.position = null;
        }

        public Optional<Vector3d> getPosition() {
            if (entity != null) {
                return Optional.of(entity.getPositionVec());
            }
            return Optional.ofNullable(position);
        }

    }

    @Nullable
    public static LivingOrPosition getPositionTarget(LivingEntity caster, float distance, BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        RayTraceResult lookingAt = RayTraceUtils.getLookingAt(LivingEntity.class, caster, distance,
                e -> validTargetChecker == null || (e != null && validTargetChecker.test(caster, e)));

        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.ENTITY) {
            EntityRayTraceResult traceResult = (EntityRayTraceResult) lookingAt;
            Entity entityHit = traceResult.getEntity();
            if (entityHit instanceof LivingEntity) {
                if (validTargetChecker != null && !validTargetChecker.test(caster, (LivingEntity) entityHit)) {
                    return null;
                }
                return new LivingOrPosition((LivingEntity) entityHit);
            }
        } else if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.BLOCK) {
            return new LivingOrPosition(lookingAt.getHitVec());
        }
        return null;
    }

    public static <E extends LivingEntity> E getSingleLivingTarget(Class<E> clazz, LivingEntity caster, float distance,
                                                                   BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        RayTraceResult lookingAt = RayTraceUtils.getLookingAt(clazz, caster, distance,
                e -> validTargetChecker == null || (e != null && validTargetChecker.test(caster, e)));

        if (lookingAt != null && lookingAt.getType() == RayTraceResult.Type.ENTITY) {

            EntityRayTraceResult traceResult = (EntityRayTraceResult) lookingAt;
            Entity entityHit = traceResult.getEntity();
            if (entityHit instanceof LivingEntity) {

                if (validTargetChecker != null && !validTargetChecker.test(caster, (LivingEntity) entityHit)) {
                    return null;
                }

                //noinspection unchecked
                return (E) entityHit;
            }
        }

        return null;
    }

    @Nonnull
    public static LivingEntity getSingleLivingTargetOrSelf(LivingEntity caster, float distance,
                                                           BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        LivingEntity target = getSingleLivingTarget(caster, distance, validTargetChecker);
        return target != null ? target : caster;
    }

    public static List<LivingEntity> getTargetsInLine(LivingEntity caster, Vector3d from, Vector3d to, float growth,
                                                      BiPredicate<LivingEntity, LivingEntity> validTargetChecker) {
        return getEntitiesInLine(LivingEntity.class, caster, from, to, Vector3d.ZERO, growth,
                e -> validTargetChecker == null || (e != null && validTargetChecker.test(caster, e)));
    }
}
