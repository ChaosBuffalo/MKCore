package com.chaosbuffalo.mkcore.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public abstract class PassivePeriodicEffect extends PassiveEffect {
    private final int period;
    private final boolean isPeriodic;

    protected PassivePeriodicEffect(EffectType effectType, int color) {
        super(effectType, color);
        period = 1;
        this.isPeriodic = false;
    }

    protected PassivePeriodicEffect(EffectType effectType, int color, int period) {
        super(effectType, color);
        this.period = period;
        this.isPeriodic = true;
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        return super.isReady(duration, amplitude) || (isPeriodic() && duration % getPeriod() == 0);
    }

    @Override
    public void doEffect(Entity source, Entity indirectSource, LivingEntity target, int amplifier, SpellCast cast) {
        if (!attemptInfiniteEffectRefresh(target, this, getPeriod()) || !isPeriodic())
            return;
        periodicEffect(source, indirectSource, target, amplifier, cast);
    }

    public void periodicEffect(Entity source, Entity indirectSource, LivingEntity target,
                               int amplifier, SpellCast cast) {

    }

    public boolean isPeriodic() {
        return isPeriodic;
    }

    public int getPeriod() {
        return period;
    }
}