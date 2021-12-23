package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class PlayerDeathTriggers extends SpellTriggers.EffectBasedTriggerCollection<PlayerDeathTriggers.PlayerDeathTrigger> {
    @FunctionalInterface
    public interface PlayerDeathTrigger {
        void apply(LivingDeathEvent event, DamageSource source, PlayerEntity player);
    }

    private static final String TAG = "PLAYER_DEATH";

    public void onEntityDeath(LivingDeathEvent event, DamageSource source, PlayerEntity entity) {
        runTrigger(entity, TAG, (trigger, instance) -> trigger.apply(event, source, entity));
    }
}
