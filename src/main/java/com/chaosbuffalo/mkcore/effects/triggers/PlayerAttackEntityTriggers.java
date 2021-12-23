package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public class PlayerAttackEntityTriggers extends SpellTriggers.EffectBasedTriggerCollection<PlayerAttackEntityTriggers.PlayerAttackEntityTrigger> {
    @FunctionalInterface
    public interface PlayerAttackEntityTrigger {
        void apply(LivingEntity player, Entity target, EffectInstance effect);
    }

    private static final String TAG = "PLAYER_ATTACK_ENTITY";

    public void onAttackEntity(LivingEntity attacker, Entity target) {
        runTrigger(attacker, TAG, (trigger, instance) -> trigger.apply(attacker, target, instance));
    }
}
