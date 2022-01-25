package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.triggers.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpellTriggers {


    public static boolean isMKDamage(DamageSource source) {
        return source instanceof MKDamageSource;
    }

    public static boolean isMinecraftPhysicalDamage(DamageSource source) {
        return !source.isFireDamage() &&
                !source.isExplosion() &&
                !source.isMagicDamage() &&
                (source.getDamageType().equals("player") || source.getDamageType().equals("mob"));
    }

    public static boolean isMeleeDamage(DamageSource source){
        return isMinecraftPhysicalDamage(source) ||
                (source instanceof MKDamageSource && ((MKDamageSource) source).isMeleeDamage());
    }

    public static boolean isSpellDamage(DamageSource source){
        return source instanceof MKDamageSource && !((MKDamageSource) source).isMeleeDamage();
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source.isProjectile();
    }

    public static boolean isNonMKProjectileDamage(DamageSource source) {
        return isProjectileDamage(source) && !isMKDamage(source);
    }


    public static final FallTriggers FALL = new FallTriggers();
    public static final PlayerHurtEntityTriggers PLAYER_HURT_ENTITY = new PlayerHurtEntityTriggers();
    public static final EntityHurtPlayerTriggers ENTITY_HURT_PLAYER = new EntityHurtPlayerTriggers();
    public static final AttackEntityTriggers ATTACK_ENTITY = new AttackEntityTriggers();
    public static final PlayerAttackEntityTriggers PLAYER_ATTACK_ENTITY = new PlayerAttackEntityTriggers();
    public static final EmptyLeftClickTriggers EMPTY_LEFT_CLICK = new EmptyLeftClickTriggers();
    public static final LivingKillEntityTriggers LIVING_KILL_ENTITY = new LivingKillEntityTriggers();
    public static final PlayerDeathTriggers PLAYER_DEATH = new PlayerDeathTriggers();
    public static final PlayerEquipmentChangeTriggers PLAYER_EQUIPMENT_CHANGE = new PlayerEquipmentChangeTriggers();

    public static abstract class TriggerCollectionBase {

        public abstract boolean hasTriggers();

        // true = trigger already active or not needed
        // false = trigger started
        protected boolean startTrigger(Entity source, String tag) {
            if (!hasTriggers())
                return true;

            if (source instanceof PlayerEntity) {
                return MKCore.getPlayer(source).map(cap -> {
                    if (cap.getCombatExtension().hasSpellTag(tag)) {
                        return true;
                    }
                    cap.getCombatExtension().addSpellTag(tag);
                    return false;
                }).orElse(true);
            }
            return true;
        }

        protected void endTrigger(Entity source, String tag) {
            if (source instanceof PlayerEntity) {
                MKCore.getPlayer(source).ifPresent(cap -> cap.getCombatExtension().removeSpellTag(tag));
            }
        }
    }

    public static abstract class EffectBasedTriggerCollection<TTrigger> extends TriggerCollectionBase {
        protected final Map<MKEffect, TTrigger> effectTriggers = new HashMap<>();

        @Override
        public boolean hasTriggers() {
            return effectTriggers.size() > 0;
        }

        public void register(MKEffect potion, TTrigger trigger) {
            effectTriggers.put(potion, trigger);
        }

        protected void runTrigger(LivingEntity entity, String tag, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            if (startTrigger(entity, tag))
                return;

            dispatchTriggers(entity, consumer);

            endTrigger(entity, tag);
        }

        private void dispatchTriggers(LivingEntity entity, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            MKCore.getEntityData(entity).ifPresent(targetData -> {
                for (MKActiveEffect effect : targetData.getEffects().effects()) {
                    TTrigger trigger = effectTriggers.get(effect.getEffect());
                    if (trigger != null) {
                        consumer.accept(trigger, effect);
                    }
                }
            });
        }
    }
}
