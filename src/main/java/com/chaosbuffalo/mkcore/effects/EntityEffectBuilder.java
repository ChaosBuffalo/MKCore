package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.mkcore.entities.PointEffectEntity;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class EntityEffectBuilder<T extends BaseEffectEntity> {

    protected final T effect;

    private EntityEffectBuilder(LivingEntity caster, Entity center, Vector3d offset) {
        this(caster, center.getPositionVec().add(offset));
    }

    private EntityEffectBuilder(LivingEntity caster, Vector3d position) {
        effect = createEntity(caster.getEntityWorld(), position);
        effect.setOwner(caster);
    }

    protected abstract T createEntity(World world, Vector3d pos);

    public EntityEffectBuilder<T> duration(int duration) {
        effect.setDuration(duration);
        return this;
    }

    public EntityEffectBuilder<T> instant() {
        return duration(6).waitTime(0);
    }


    public EntityEffectBuilder<T> waitTime(int waitTime) {
        effect.setWaitTime(waitTime);
        return this;
    }

    public EntityEffectBuilder<T> tickRate(int tickRate){
        effect.setTickRate(tickRate);
        return this;
    }

    public EntityEffectBuilder<T> setParticles(ResourceLocation animation){
        effect.setParticles(animation);
        return this;
    }

    public EntityEffectBuilder<T> setWaitingParticles(ResourceLocation animation){
        effect.setWaitingParticles(animation);
        return this;
    }

    public EntityEffectBuilder<T> setParticles(BaseEffectEntity.ParticleDisplay display) {
        effect.setParticles(display);
        return this;
    }

    public EntityEffectBuilder<T> setWaitingParticles(BaseEffectEntity.ParticleDisplay display) {
        effect.setWaitingParticles(display);
        return this;
    }

    public EntityEffectBuilder<T> delayedEffect(EffectInstance effect, TargetingContext targetContext, int delayTicks) {
        this.effect.addDelayedEffect(effect, targetContext, delayTicks);
        return this;
    }

    public EntityEffectBuilder<T> delayedEffect(MKEffectBuilder<?> effect, TargetingContext targetContext, int delayTicks) {
        this.effect.addDelayedEffect(effect, targetContext, delayTicks);
        return this;
    }

    public EntityEffectBuilder<T> effect(EffectInstance effect, TargetingContext targetContext) {
        this.effect.addEffect(effect, targetContext);
        return this;
    }

    public EntityEffectBuilder<T> effect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effect.addEffect(effect, targetContext);
        return this;
    }


    public void spawn() {
        if (effect.getOwner() != null) {
            effect.getOwner().world.addEntity(effect);
        }
    }

    public static class LineEffectBuilder extends EntityEffectBuilder<LineEffectEntity> {

        private LineEffectBuilder(LivingEntity caster, Entity center, Vector3d startPoint, Vector3d endPoint) {
            super(caster, center, Vector3d.ZERO);
            effect.setStartPoint(startPoint);
            effect.setEndPoint(endPoint);
        }

        private LineEffectBuilder(LivingEntity caster, Vector3d startPoint, Vector3d endPoint){
            super(caster, startPoint);
            effect.setStartPoint(startPoint);
            effect.setEndPoint(endPoint);
        }

        @Override
        protected LineEffectEntity createEntity(World world, Vector3d pos) {
            return new LineEffectEntity(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static LineEffectBuilder createLineEffectOnEntity(LivingEntity caster, Entity center, Vector3d start, Vector3d end) {
        return new LineEffectBuilder(caster, center, start, end);
    }

    public static LineEffectBuilder createLineEffect(LivingEntity caster, Vector3d start, Vector3d end) {
        return new LineEffectBuilder(caster, start, end);
    }

    public static class PointEffectBuilder extends EntityEffectBuilder<PointEffectEntity> {

        private PointEffectBuilder(LivingEntity caster, Entity center, Vector3d offset) {
            super(caster, center, offset);
        }

        private PointEffectBuilder(LivingEntity caster, Vector3d position){
            super(caster, position);
        }

        @Override
        protected PointEffectEntity createEntity(World world, Vector3d pos) {
            return new PointEffectEntity(world, pos.getX(), pos.getY(), pos.getZ());
        }

        public PointEffectBuilder radius(float radius) {
            effect.setRadius(radius);
            return this;
        }

    }

    public static PointEffectBuilder createPointEffectOnEntity(LivingEntity caster, Entity center, Vector3d offset) {
        return new PointEffectBuilder(caster, center, offset);
    }

    public static PointEffectBuilder createPointEffect(LivingEntity caster, Vector3d position) {
        return new PointEffectBuilder(caster, position);
    }
}
