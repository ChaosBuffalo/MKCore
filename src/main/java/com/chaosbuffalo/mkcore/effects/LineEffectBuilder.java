package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class LineEffectBuilder {

    private final LineEffectEntity lineEffect;

    private LineEffectBuilder(LivingEntity caster, Entity center, Vector3d startPoint, Vector3d endPoint) {
        lineEffect = new LineEffectEntity(center.getEntityWorld(), center.getPosX(),
                center.getPosY(), center.getPosZ());
        lineEffect.setOwner(caster);
        lineEffect.setStartPoint(startPoint);
        lineEffect.setEndPoint(endPoint);
    }

    public static LineEffectBuilder createOnEntity(LivingEntity caster, Entity center, Vector3d start, Vector3d end) {
        return new LineEffectBuilder(caster, center, start, end);
    }

    public LineEffectBuilder duration(int duration) {
        lineEffect.setDuration(duration);
        return this;
    }

    public LineEffectBuilder instant() {
        return duration(6).waitTime(0);
    }


    public LineEffectBuilder waitTime(int waitTime) {
        lineEffect.setWaitTime(waitTime);
        return this;
    }

    public LineEffectBuilder tickRate(int tickRate){
        lineEffect.setTickRate(tickRate);
        return this;
    }

    public LineEffectBuilder visualTickRate(int tickRate){
        lineEffect.setVisualTickRate(tickRate);
        return this;
    }

    public LineEffectBuilder setParticles(ResourceLocation animation){
        lineEffect.setParticles(animation);
        return this;
    }

    public LineEffectBuilder setWaitingParticles(ResourceLocation animation){
        lineEffect.setWaitingParticles(animation);
        return this;
    }

    public LineEffectBuilder effect(EffectInstance effect, TargetingContext targetContext) {
        lineEffect.addEffect(effect, targetContext);
        return this;
    }

    public LineEffectBuilder effect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        lineEffect.addEffect(effect, targetContext);
        return this;
    }


    public void spawn() {
        if (lineEffect.getOwner() != null) {
            lineEffect.getOwner().world.addEntity(lineEffect);
        }
    }
}
