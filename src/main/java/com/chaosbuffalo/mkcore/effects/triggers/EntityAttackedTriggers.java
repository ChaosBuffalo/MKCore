package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class EntityAttackedTriggers extends SpellTriggers.EffectBasedTriggerCollection<EntityAttackedTriggers.Trigger> {
    @FunctionalInterface
    public interface Trigger {
        void apply(LivingEntity player, Entity target, MKActiveEffect effect);
    }

    private static final String TAG = "ATTACK_ENTITY";

    public void onAttackEntity(LivingEntity attacker, Entity target) {
        runTrigger(attacker, TAG, (trigger, instance) -> trigger.apply(attacker, target, instance));
    }
}
