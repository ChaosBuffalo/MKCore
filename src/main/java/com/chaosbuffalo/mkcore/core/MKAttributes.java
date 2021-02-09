package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

public class MKAttributes {

    // Players Only Attributes
    public static final RangedAttribute MAX_MANA = (RangedAttribute) new RangedAttribute("attribute.name.mk.max_mana", 0, 0, 1024)
            .setRegistryName(MKCore.makeRL("max_mana"))
            .setShouldWatch(true);

    public static final RangedAttribute MANA_REGEN = (RangedAttribute) new RangedAttribute("attribute.name.mk.mana_regen", 0, 0, 1024)
            .setRegistryName(MKCore.makeRL("mana_regen"))
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRIT = (RangedAttribute) new MKRangedAttribute("attribute.name.mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("melee_crit_chance"))
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("attribute.name.mk.melee_crit_multiplier", 0.0, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("melee_crit_multiplier"))
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRIT = (RangedAttribute) new MKRangedAttribute("attribute.name.mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("spell_crit_chance"))
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("attribute.name.mk.spell_crit_multiplier", 1.5, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("spell_crit_multiplier"))
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_CRIT = (RangedAttribute) new MKRangedAttribute("attribute.name.mk.ranged_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("ranged_crit_chance"))
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("attribute.name.mk.ranged_crit_multiplier", 0.0, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("ranged_crit_multiplier"))
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.ranged_damage", 0.0, 0.0, 2048)
            .setRegistryName(MKCore.makeRL("ranged_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.ranged_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("ranged_resistance"))
            .setShouldWatch(true);

    // Everyone Attributes

    // This is slightly confusing.
    // 1.5 max means the cooldown will progress at most 50% faster than the normal rate. This translates into a 50% reduction in the observed cooldown.
    // 0.25 minimum means that a cooldown can be increased up to 175% of the normal value. This translates into a 75% increase in the observed cooldown
    public static final RangedAttribute COOLDOWN = (RangedAttribute) new RangedAttribute("attribute.name.mk.cooldown_rate", 1, 0.25, 1.5)
            .setRegistryName(MKCore.makeRL("cooldown_rate"))
            .setShouldWatch(true);

    public static final RangedAttribute HEAL_BONUS = (RangedAttribute) new RangedAttribute("attribute.name.mk.heal_bonus", 1.0, 0.0, 2.0)
            .setRegistryName(MKCore.makeRL("heal_bonus"))
            .setShouldWatch(true);

    public static final RangedAttribute CASTING_SPEED = (RangedAttribute) new RangedAttribute("attribute.name.mk.casting_speed", 1, 0.25, 1.5)
            .setRegistryName(MKCore.makeRL("casting_speed"))
            .setShouldWatch(true);

    public static final RangedAttribute BUFF_DURATION = (RangedAttribute) new RangedAttribute("attribute.name.mk.buff_duration", 1.0, 0.0, 5.0)
            .setRegistryName(MKCore.makeRL("buff_duration"))
            .setShouldWatch(true);

    public static final RangedAttribute ELEMENTAL_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.elemental_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("elemental_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute ELEMENTAL_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.elemental_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("elemental_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.arcane_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("arcane_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.arcane_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("arcane_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.fire_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("fire_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.fire_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("fire_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute FROST_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.frost_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("frost_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute FROST_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.frost_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("frost_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.shadow_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("shadow_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.shadow_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("shadow_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.holy_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("holy_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.holy_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("holy_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute NATURE_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.nature_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("nature_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute NATURE_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.nature_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("nature_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute POISON_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.poison_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("poison_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute POISON_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.poison_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("poison_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute BLEED_RESISTANCE = (RangedAttribute) new RangedAttribute("attribute.name.mk.bleed_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("bleed_resistance"))
            .setShouldWatch(true);

    public static final RangedAttribute BLEED_DAMAGE = (RangedAttribute) new RangedAttribute("attribute.name.mk.bleed_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("bleed_damage"))
            .setShouldWatch(true);

    public static final RangedAttribute ATTACK_REACH = (RangedAttribute) new RangedAttribute("attribute.name.mk.attack_reach", 3.0, 0.0, 128)
            .setRegistryName(MKCore.makeRL("attack_reach"))
            .setShouldWatch(true);

    public static final RangedAttribute ABJURATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.abjuration", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("abjuration"))
            .setShouldWatch(true);

    public static final RangedAttribute ALTERATON = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.alteration", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("alteration"))
            .setShouldWatch(true);

    public static final RangedAttribute CONJURATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.conjuration", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("conjuration"))
            .setShouldWatch(true);

    public static final RangedAttribute DIVINATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.divination", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("divination"))
            .setShouldWatch(true);

    public static final RangedAttribute ENCHANTMENT = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.enchantment", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("enchantment"))
            .setShouldWatch(true);

    public static final RangedAttribute PHANTASM = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.phantasm", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("phantasm"))
            .setShouldWatch(true);

    public static final RangedAttribute NECROMANCY = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.necromancy", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("necromancy"))
            .setShouldWatch(true);

    public static final RangedAttribute RESTORATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.restoration", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("restoration"))
            .setShouldWatch(true);

    public static final RangedAttribute ARETE = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.arete", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("arete"))
            .setShouldWatch(true);

    public static final RangedAttribute PNEUMA = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.pneuma", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("pneuma"))
            .setShouldWatch(true);

    public static final RangedAttribute PANKRATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.pankration", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("pankration"))
            .setShouldWatch(true);

    public static final RangedAttribute EVOCATION = (RangedAttribute) new RangedAttribute(
            "attribute.name.mk.evocation", 0, -10, 10)
            .setRegistryName(MKCore.makeRL("evocation"))
            .setShouldWatch(true);


    public static void iterateEntityAttributes(Consumer<Attribute> consumer) {
        consumer.accept(COOLDOWN);
        consumer.accept(CASTING_SPEED);
        consumer.accept(HEAL_BONUS);
        consumer.accept(BUFF_DURATION);
        consumer.accept(ATTACK_REACH);
        consumer.accept(ELEMENTAL_DAMAGE);
        consumer.accept(ELEMENTAL_RESISTANCE);
        consumer.accept(ARCANE_DAMAGE);
        consumer.accept(ARCANE_RESISTANCE);
        consumer.accept(FIRE_DAMAGE);
        consumer.accept(FIRE_RESISTANCE);
        consumer.accept(FROST_DAMAGE);
        consumer.accept(FROST_RESISTANCE);
        consumer.accept(SHADOW_DAMAGE);
        consumer.accept(SHADOW_RESISTANCE);
        consumer.accept(HOLY_DAMAGE);
        consumer.accept(HOLY_RESISTANCE);
        consumer.accept(NATURE_DAMAGE);
        consumer.accept(NATURE_RESISTANCE);
        consumer.accept(POISON_DAMAGE);
        consumer.accept(POISON_RESISTANCE);
        consumer.accept(BLEED_DAMAGE);
        consumer.accept(BLEED_RESISTANCE);
        consumer.accept(RANGED_DAMAGE);
        consumer.accept(RANGED_RESISTANCE);

        consumer.accept(ABJURATION);
        consumer.accept(ALTERATON);
        consumer.accept(CONJURATION);
        consumer.accept(DIVINATION);
        consumer.accept(ENCHANTMENT);
        consumer.accept(PHANTASM);
        consumer.accept(NECROMANCY);
        consumer.accept(RESTORATION);
        consumer.accept(ARETE);
        consumer.accept(PNEUMA);
        consumer.accept(PANKRATION);
        consumer.accept(EVOCATION);
    }

    public static void iteratePlayerAttributes(Consumer<Attribute> consumer) {
        consumer.accept(MAX_MANA);
        consumer.accept(MANA_REGEN);
        consumer.accept(MELEE_CRIT);
        consumer.accept(MELEE_CRIT_MULTIPLIER);
        consumer.accept(SPELL_CRIT);
        consumer.accept(SPELL_CRIT_MULTIPLIER);

        consumer.accept(RANGED_CRIT);
        consumer.accept(RANGED_CRIT_MULTIPLIER);
        consumer.accept(RANGED_DAMAGE);
        consumer.accept(RANGED_RESISTANCE);

        consumer.accept(ABJURATION);
        consumer.accept(ALTERATON);
        consumer.accept(CONJURATION);
        consumer.accept(DIVINATION);
        consumer.accept(ENCHANTMENT);
        consumer.accept(PHANTASM);
        consumer.accept(NECROMANCY);
        consumer.accept(RESTORATION);
        consumer.accept(ARETE);
        consumer.accept(PNEUMA);
        consumer.accept(PANKRATION);
        consumer.accept(EVOCATION);

    }

    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {

        @SubscribeEvent
        public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
            iterateEntityAttributes(event.getRegistry()::register);
            iteratePlayerAttributes(event.getRegistry()::register);
        }
    }
}
