package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityHurtPlayerTriggers extends SpellTriggers.TriggerCollectionBase {
    @FunctionalInterface
    public interface Trigger {
        void apply(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget, MKPlayerData targetData);
    }

    private static final String TAG = "ENTITY_HURT_PLAYER";
    private final List<Trigger> entityHurtPlayerPreTriggers = new ArrayList<>();
    private final List<Trigger> entityHurtPlayerPostTriggers = new ArrayList<>();

    @Override
    public boolean hasTriggers() {
        return entityHurtPlayerPreTriggers.size() > 0 || entityHurtPlayerPostTriggers.size() > 0;
    }

    public void registerPreScale(Trigger trigger) {
        entityHurtPlayerPreTriggers.add(trigger);
    }

    public void registerPostScale(Trigger trigger) {
        entityHurtPlayerPostTriggers.add(trigger);
    }

    public void onEntityHurtPlayer(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget,
                                   MKPlayerData targetData) {
        if (startTrigger(livingTarget, TAG))
            return;
        entityHurtPlayerPreTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));

        if (SpellTriggers.isMKDamage(source)) {
            MKDamageSource mkDamageSource = (MKDamageSource) source;
            if (mkDamageSource.isUnblockable()) {
                event.setAmount(mkDamageSource.getMKDamageType().applyResistance(livingTarget, event.getAmount()));
            }
        }

        entityHurtPlayerPostTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));
        endTrigger(livingTarget, TAG);
    }
}
