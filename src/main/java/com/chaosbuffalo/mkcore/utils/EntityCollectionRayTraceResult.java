package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;

public class EntityCollectionRayTraceResult<E extends Entity> extends EntityRayTraceResult {

    private final List<E> entities;

    public EntityCollectionRayTraceResult(E nearest, List<E> entities) {
        super(nearest, nearest == null ? new Vector3d(0.0, 0.0, 0.0) : nearest.getPositionVec());
        this.entities = entities;
    }

    public List<E> getEntities() {
        return entities;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return super.getEntity();
    }
}
