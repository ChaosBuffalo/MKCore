package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.events.ServerSideLeftClickEmpty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;

public class EmptyLeftClickTriggers extends SpellTriggers.EffectBasedTriggerCollection<EmptyLeftClickTriggers.Trigger> {
    @FunctionalInterface
    public interface Trigger {
        void apply(ServerSideLeftClickEmpty event, PlayerEntity player, EffectInstance effect);
    }

    private static final String TAG = "EMPTY_LEFT_CLICK";

    public void onEmptyLeftClick(PlayerEntity player, ServerSideLeftClickEmpty event) {
        runTrigger(player, TAG, (trigger, instance) -> trigger.apply(event, player, instance));
    }
}
