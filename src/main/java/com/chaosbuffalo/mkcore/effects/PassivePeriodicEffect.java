package com.chaosbuffalo.mkcore.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public abstract class PassivePeriodicEffect extends PassiveEffect {
    private final int period;
    private final boolean doPeriodicTick;

    protected PassivePeriodicEffect(EffectType typeIn, int liquidColorIn) {
        this(typeIn, liquidColorIn, 1, false);
    }

    protected PassivePeriodicEffect(EffectType typeIn, int liquidColorIn, int periodIn, boolean doPeriodicTickIn){
        super(typeIn, liquidColorIn);
        this.period = periodIn;
        this.doPeriodicTick = doPeriodicTickIn;
    }

    @Override
    public boolean isReady(int duration, int amplitude) {
        return super.isReady(duration, amplitude) || (isPeriodic() && duration % getPeriod() == 0);
    }

    @Override
    public void doEffect(Entity source, Entity indirectSource, LivingEntity target, int amplifier, SpellCast cast) {
        if (!attemptInfiniteEffectRefresh(target, this, getPeriod()) || !isPeriodic())
            return;
        periodicEffect(source, indirectSource, target ,amplifier, cast);
    }

    public void periodicEffect(Entity source, Entity indirectSource, LivingEntity target,
                               int amplifier, SpellCast cast){

    }

    public boolean isPeriodic(){
        return doPeriodicTick;
    }

    public int getPeriod() {
        return period;
    }
}
