package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class RayTraceUtils {

    private static final Predicate<Entity> defaultFilter = e -> EntityPredicates.IS_ALIVE.test(e) && EntityPredicates.NOT_SPECTATING.test(e);


    public static Vector3d getPerpendicular(Vector3d vec) {
        Vector3d cVec;
        if (vec.y != 0 || vec.z != 0) {
            cVec = new Vector3d(1, 0, 0);
        } else {
            cVec = new Vector3d(0, 1, 0);
        }
        return vec.crossProduct(cVec);
    }

    public static <E extends Entity> List<E> getEntitiesInLine(Class<E> clazz, final Entity mainEntity,
                                                               Vector3d from, Vector3d to, Vector3d expansion,
                                                               float growth, final Predicate<E> filter) {
        Predicate<E> predicate = e -> defaultFilter.test(e) && filter.test(e);
        AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(from), new BlockPos(to))
                .expand(expansion.x, expansion.y, expansion.z)
                .grow(growth);
        List<E> entities = mainEntity.getEntityWorld().getEntitiesWithinAABB(clazz, bb, predicate);
        return entities;
    }

    public static <E extends Entity> RayTraceResult getLookingAt(Class<E> clazz, final Entity mainEntity, double distance, final Predicate<E> entityPredicate) {

        Predicate<E> finalFilter = e -> e != mainEntity &&
                defaultFilter.test(e) &&
                e.canBeCollidedWith() &&
                entityPredicate.test(e);

        RayTraceResult position = null;

        if (mainEntity.world != null) {
            Vector3d look = mainEntity.getLookVec().scale(distance);
            Vector3d from = mainEntity.getPositionVec().add(0, mainEntity.getEyeHeight(), 0);
            Vector3d to = from.add(look);
            position = rayTraceBlocksAndEntities(clazz, mainEntity, from, to, false, finalFilter);
        }
        return position;
    }

    public static BlockRayTraceResult rayTraceBlocks(Entity entity, Vector3d from, Vector3d to, boolean stopOnLiquid) {
        RayTraceContext.FluidMode mode = stopOnLiquid ? RayTraceContext.FluidMode.SOURCE_ONLY : RayTraceContext.FluidMode.NONE;
        RayTraceContext context = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, mode, entity);
        return entity.getEntityWorld().rayTraceBlocks(context);
    }

    public static RayTraceResult rayTraceEntities(World world, Vector3d from, Vector3d to, Vector3d aaExpansion, float aaGrowth,
                                                  float entityExpansion, final Predicate<Entity> filter) {
        return rayTraceEntities(Entity.class, world, from, to, aaExpansion, aaGrowth, entityExpansion, filter);
    }

    public static <E extends Entity> EntityRayTraceResult rayTraceEntities(Class<E> clazz, World world,
                                                                           Vector3d from, Vector3d to,
                                                                           Vector3d aaExpansion,
                                                                           float aaGrowth,
                                                                           float entityExpansion,
                                                                           final Predicate<E> filter) {

        Predicate<E> predicate = input -> defaultFilter.test(input) && filter.test(input);

        Entity nearest = null;
        double distance = 0;

        AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(from), new BlockPos(to))
                .expand(aaExpansion.x, aaExpansion.y, aaExpansion.z)
                .grow(aaGrowth);
        List<E> entities = world.getEntitiesWithinAABB(clazz, bb, predicate);
        for (Entity entity : entities) {
            AxisAlignedBB entityBB = entity.getBoundingBox().grow(entityExpansion);
            Optional<Vector3d> intercept = entityBB.rayTrace(from, to);
            if (intercept.isPresent()) {
                double dist = from.distanceTo(intercept.get());
                if (dist < distance || distance == 0.0D) {
                    nearest = entity;
                    distance = dist;
                }
            }
        }

        if (nearest != null)
            return new EntityRayTraceResult(nearest);
        return null;
    }

    private static <E extends Entity> RayTraceResult rayTraceBlocksAndEntities(Class<E> clazz, Entity mainEntity,
                                                                               Vector3d from, Vector3d to, boolean stopOnLiquid,
                                                                               final Predicate<E> entityFilter) {
        BlockRayTraceResult block = rayTraceBlocks(mainEntity, from, to, stopOnLiquid);
        if (block.getType() == RayTraceResult.Type.BLOCK)
            to = block.getHitVec();

        EntityRayTraceResult entity = rayTraceEntities(clazz, mainEntity.getEntityWorld(), from, to, Vector3d.ZERO, 0.5f, 0.5f, entityFilter);

        if (block.getType() == RayTraceResult.Type.MISS) {
            return entity;
        } else {
            if (entity == null) {
                return block;
            } else {
                double blockDist = block.getHitVec().distanceTo(from);
                double entityDist = entity.getHitVec().distanceTo(from);
                if (blockDist < entityDist) {
                    return block;
                } else {
                    return entity;
                }
            }
        }
    }
}
