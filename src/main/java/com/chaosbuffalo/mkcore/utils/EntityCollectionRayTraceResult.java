package com.chaosbuffalo.mkcore.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class EntityCollectionRayTraceResult<E extends Entity> {

    public static class TraceEntry<E>{
        public E entity;
        public double distance;
        public Vector3d intercept;

        public TraceEntry(E entity, double distance, Vector3d intercept){
            this.entity = entity;
            this.distance = distance;
            this.intercept = intercept;
        }
    }

    private final List<TraceEntry<E>> entities;

    public EntityCollectionRayTraceResult(List<TraceEntry<E>> entities) {
        this.entities = entities;
    }

    public List<TraceEntry<E>> getEntities() {
        return entities;
    }

}
