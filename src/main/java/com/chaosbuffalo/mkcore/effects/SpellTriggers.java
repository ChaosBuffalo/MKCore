package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.triggers.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;

import java.util.*;
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


    public static FallTriggers FALL = new FallTriggers();
    public static PlayerHurtEntityTriggers PLAYER_HURT_ENTITY = new PlayerHurtEntityTriggers();
    public static EntityHurtPlayerTriggers ENTITY_HURT_PLAYER = new EntityHurtPlayerTriggers();
    public static AttackEntityTriggers ATTACK_ENTITY = new AttackEntityTriggers();
    public static PlayerAttackEntityTriggers PLAYER_ATTACK_ENTITY = new PlayerAttackEntityTriggers();
    public static EmptyLeftClickTriggers EMPTY_LEFT_CLICK = new EmptyLeftClickTriggers();
    public static LivingKillEntityTriggers LIVING_KILL_ENTITY = new LivingKillEntityTriggers();
    public static PlayerDeathTriggers PLAYER_DEATH = new PlayerDeathTriggers();
    public static PlayerEquipmentChangeTriggers PLAYER_EQUIPMENT_CHANGE = new PlayerEquipmentChangeTriggers();

    public static abstract class TriggerCollectionBase {

        public abstract boolean hasTriggers();

        protected boolean startTrigger(Entity source, String tag) {
            if (!hasTriggers())
                return true;

            if (source instanceof PlayerEntity) {
                return MKCore.getPlayer(source).map(cap -> {
                    if (cap.getCombatExtension().hasSpellTag(tag)) {
                        return false;
                    }
                    cap.getCombatExtension().addSpellTag(tag);
                    return true;
                }).orElse(true);
            }
            return false;
        }

        protected void endTrigger(Entity source, String tag) {
            if (source instanceof PlayerEntity) {
                MKCore.getPlayer(source).ifPresent(cap -> cap.getCombatExtension().removeSpellTag(tag));
            }
        }
    }

    public static abstract class EffectBasedTriggerCollection<TTrigger> extends TriggerCollectionBase {
        protected final Map<SpellEffectBase, TTrigger> effectTriggers = new HashMap<>();

        @Override
        public boolean hasTriggers() {
            return effectTriggers.size() > 0;
        }

        public void register(SpellEffectBase potion, TTrigger trigger) {
            effectTriggers.put(potion, trigger);
        }

        protected void runTrigger(LivingEntity entity, String tag, BiConsumer<TTrigger, EffectInstance> consumer) {
            if (startTrigger(entity, tag))
                return;

            dispatchTriggers(entity, consumer);

            endTrigger(entity, tag);
        }

        private void dispatchTriggers(LivingEntity entity, BiConsumer<TTrigger, EffectInstance> consumer) {
            for (EffectInstance effectInstance : entity.getActivePotionEffects()) {
                if (effectInstance.getPotion() instanceof SpellEffectBase) {
                    SpellEffectBase effect = (SpellEffectBase) effectInstance.getPotion();
                    TTrigger trigger = effectTriggers.get(effect);
                    if (trigger != null) {
                        consumer.accept(trigger, effectInstance);
                    }
                }
            }
        }
    }
}