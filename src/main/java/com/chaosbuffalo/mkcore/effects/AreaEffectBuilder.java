package com.chaosbuffalo.mkcore.effects;


import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;

public class AreaEffectBuilder {

    private final MKAreaEffectEntity areaEffectCloud;

    private AreaEffectBuilder(LivingEntity caster, Entity center) {
        areaEffectCloud = new MKAreaEffectEntity(center.getEntityWorld(), center.getPosX(), center.getPosY(), center.getPosZ());
        areaEffectCloud.setOwner(caster);
    }

    @Deprecated
    public static AreaEffectBuilder Create(LivingEntity caster, Entity center) {
        return new AreaEffectBuilder(caster, center);
    }

    public static AreaEffectBuilder createOnEntity(LivingEntity caster, Entity center) {
        return new AreaEffectBuilder(caster, center);
    }

    public static AreaEffectBuilder createOnCaster(LivingEntity caster) {
        return createOnEntity(caster, caster);
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

    public AreaEffectBuilder effect(EffectInstance effect, TargetingContext targetContext) {
        areaEffectCloud.addEffect(effect, targetContext);
        return this;
    }

    public AreaEffectBuilder effect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        areaEffectCloud.addEffect(effect, targetContext);
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
        areaEffectCloud.setParticleData(particleType);
        return this;
    }

    public AreaEffectBuilder disableParticle() {
        areaEffectCloud.disableParticle();
        return this;
    }

    public AreaEffectBuilder period(int ticksBetweenApplication) {
        areaEffectCloud.setPeriod(ticksBetweenApplication);
        return this;
    }

    public void spawn() {
        if (areaEffectCloud.getOwner() != null) {
            areaEffectCloud.getOwner().world.addEntity(areaEffectCloud);
        }

    }
}