package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;

public class PlayerEquipmentChangeTriggers extends SpellTriggers.EffectBasedTriggerCollection<PlayerEquipmentChangeTriggers.PlayerEquipmentChangeTrigger> {
    @FunctionalInterface
    public interface PlayerEquipmentChangeTrigger {
        void apply(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player);
    }

    private static final String TAG = "PLAYER_EQUIPMENT_CHANGE";

    public void onEquipmentChange(LivingEquipmentChangeEvent event, IMKEntityData data, PlayerEntity player) {
        runTrigger(player, TAG, (trigger, instance) -> trigger.apply(event, data, player));
    }
}
