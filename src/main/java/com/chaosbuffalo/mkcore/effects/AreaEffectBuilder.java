package com.chaosbuffalo.mkcore.effects;


import com.chaosbuffalo.mkcore.entities.EntityMKAreaEffect;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;

public class AreaEffectBuilder {

    private final EntityMKAreaEffect areaEffectCloud;

    private AreaEffectBuilder(LivingEntity caster, Entity center) {
        areaEffectCloud = new EntityMKAreaEffect(center.getEntityWorld(), center.getPosX(), center.getPosY(), center.getPosZ());
        areaEffectCloud.setOwner(caster);
    }

    public static AreaEffectBuilder Create(LivingEntity caster, Entity center) {
        return new AreaEffectBuilder(caster, center);
    }

    public AreaEffectBuilder instant() {
        return duration(6).waitTime(0);
    }

    public AreaEffectBuilder duration(int duration) {
        areaEffectCloud.setDuration(duration);
        return this;
    }

    public AreaEffectBuilder waitTime(int waitTime) {
        areaEffectCloud.setWaitTime(waitTime);
        return this;
    }

    public AreaEffectBuilder spellCast(SpellCast cast, int amplifier, Targeting.TargetType targetType) {
        return spellCast(cast, cast.toPotionEffect(amplifier), targetType, false);
    }

    public AreaEffectBuilder spellCast(SpellCast cast, int amplifier, Targeting.TargetType targetType, boolean excludeCaster) {
        return spellCast(cast, cast.toPotionEffect(amplifier), targetType, excludeCaster);
    }

    public AreaEffectBuilder spellCast(SpellCast cast, int duration, int amplifier, Targeting.TargetType targetType) {
        return spellCast(cast, cast.toPotionEffect(duration, amplifier), targetType, false);
    }

    public AreaEffectBuilder spellCast(SpellCast cast, EffectInstance effect, Targeting.TargetType targetType) {
        return spellCast(cast, effect, targetType, false);
    }

    public AreaEffectBuilder spellCast(SpellCast cast, EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
        areaEffectCloud.addSpellCast(cast, effect, targetType, excludeCaster);
        return this;
    }

    public AreaEffectBuilder effect(EffectInstance effect, Targeting.TargetType targetType) {
        return effect(effect, targetType, false);
    }

    private AreaEffectBuilder effect(EffectInstance effect, Targeting.TargetType targetType, boolean excludeCaster) {
        areaEffectCloud.addEffect(effect, targetType, excludeCaster);
        return this;
    }

    public AreaEffectBuilder radius(float radius) {
        return radius(radius, false);
    }

    public AreaEffectBuilder radius(float radius, boolean makeCube) {
        areaEffectCloud.setRadius(radius);

        // setRadius calls setSize which changes the bounding box according to the width and height
        // but the default height of an AreaEffect is just 0.5
        if (makeCube) {
            AxisAlignedBB bb = areaEffectCloud.getBoundingBox();
            bb = bb.expand(0, radius, 0);
            bb = bb.expand(0, -radius, 0);
            areaEffectCloud.setBoundingBox(bb);
        }
        return this;
    }

    public AreaEffectBuilder color(int color) {
        areaEffectCloud.setColor(color);
        return this;
    }

    public AreaEffectBuilder particle(IParticleData particleType) {
        areaEffectCloud.setParticle(particleType);
        return this;
    }

    public AreaEffectBuilder disableParticle() {
        areaEffectCloud.disableParticle();
        return this;
    }

    public AreaEffectBuilder period(int ticksBetweenApplication) {
        areaEffectCloud.setReapplicationDelay(ticksBetweenApplication);
        return this;
    }

    public void spawn() {
        if (areaEffectCloud.getOwner() != null) {
            areaEffectCloud.getOwner().world.addEntity(areaEffectCloud);
        }

    }
}