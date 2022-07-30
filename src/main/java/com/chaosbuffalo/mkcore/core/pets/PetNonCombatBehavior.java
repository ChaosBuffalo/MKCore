package com.chaosbuffalo.mkcore.core.pets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Optional;

public class PetNonCombatBehavior {
    public enum Behavior {
        GUARD,
        FOLLOW
    }

    private final Behavior behaviorType;
    @Nullable
    private final LivingEntity entity;
    @Nullable
    private final Vector3d pos;

    public PetNonCombatBehavior(LivingEntity entity) {
        behaviorType = Behavior.FOLLOW;
        this.entity = entity;
        pos = null;
    }

    public PetNonCombatBehavior(Vector3d pos) {
        behaviorType = Behavior.GUARD;
        this.pos = pos;
        entity = null;
    }

    public Optional<Vector3d> getPos() {
        return Optional.ofNullable(pos);
    }

    public Optional<LivingEntity> getEntity() {
        return Optional.ofNullable(entity);
    }

    public Behavior getBehaviorType() {
        return behaviorType;
    }
}
