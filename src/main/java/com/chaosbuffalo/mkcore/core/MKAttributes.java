package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MKAttributes {

    // Players Only Attributes
    public static final Attribute MAX_MANA = new MKRangedAttribute("attribute.name.mk.max_mana", 0, 0, 1024)
            .setRegistryName(MKCore.makeRL("max_mana"))
            .setShouldWatch(true);

    public static final Attribute MANA_REGEN = new MKRangedAttribute("attribute.name.mk.mana_regen", 0, 0, 1024)
            .setRegistryName(MKCore.makeRL("mana_regen"))
            .setShouldWatch(true);

    public static final Attribute MELEE_CRIT = new MKRangedAttribute("attribute.name.mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("melee_crit_chance"))
            .setShouldWatch(true);

    public static final Attribute MELEE_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.melee_crit_multiplier", 1.0, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("melee_crit_multiplier"))
            .setShouldWatch(true);

    public static final Attribute SPELL_CRIT = new MKRangedAttribute("attribute.name.mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("spell_crit_chance"))
            .setShouldWatch(true);

    public static final Attribute SPELL_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.spell_crit_multiplier", 1.5, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("spell_crit_multiplier"))
            .setShouldWatch(true);

    public static final Attribute RANGED_CRIT = new MKRangedAttribute("attribute.name.mk.ranged_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setRegistryName(MKCore.makeRL("ranged_crit_chance"))
            .setShouldWatch(true);

    public static final Attribute RANGED_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.ranged_crit_multiplier", 0.0, 0.0, 10.0)
            .setRegistryName(MKCore.makeRL("ranged_crit_multiplier"))
            .setShouldWatch(true);


    // Everyone Attributes
    public static final Attribute RANGED_DAMAGE = new MKRangedAttribute("attribute.name.mk.ranged_damage", 0.0, 0.0, 2048)
            .setRegistryName(MKCore.makeRL("ranged_damage"))
            .setShouldWatch(true);

    public static final Attribute RANGED_RESISTANCE = new MKRangedAttribute("attribute.name.mk.ranged_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("ranged_resistance"))
            .setShouldWatch(true);

    // This is slightly confusing.
    // 1.5 max means the cooldown will progress at most 50% faster than the normal rate. This translates into a 50% reduction in the observed cooldown.
    // 0.25 minimum means that a cooldown can be increased up to 175% of the normal value. This translates into a 75% increase in the observed cooldown
    public static final Attribute COOLDOWN = new MKRangedAttribute("attribute.name.mk.cooldown_rate", 1, 0.25, 1.5)
            .setRegistryName(MKCore.makeRL("cooldown_rate"))
            .setShouldWatch(true);

    public static final Attribute HEAL_BONUS = new MKRangedAttribute("attribute.name.mk.heal_bonus", 1.0, 0.0, 2.0)
            .setRegistryName(MKCore.makeRL("heal_bonus"))
            .setShouldWatch(true);

    public static final Attribute CASTING_SPEED = new MKRangedAttribute("attribute.name.mk.casting_speed", 1, 0.25, 1.5)
            .setRegistryName(MKCore.makeRL("casting_speed"))
            .setShouldWatch(true);

    public static final Attribute BUFF_DURATION = new MKRangedAttribute("attribute.name.mk.buff_duration", 1.0, 0.0, 5.0)
            .setRegistryName(MKCore.makeRL("buff_duration"))
            .setShouldWatch(true);

    public static final Attribute ARCANE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.arcane_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("arcane_resistance"))
            .setShouldWatch(true);

    public static final Attribute ARCANE_DAMAGE = new MKRangedAttribute("attribute.name.mk.arcane_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("arcane_damage"))
            .setShouldWatch(true);

    public static final Attribute FIRE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.fire_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("fire_resistance"))
            .setShouldWatch(true);

    public static final Attribute FIRE_DAMAGE = new MKRangedAttribute("attribute.name.mk.fire_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("fire_damage"))
            .setShouldWatch(true);

    public static final Attribute FROST_RESISTANCE = new MKRangedAttribute("attribute.name.mk.frost_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("frost_resistance"))
            .setShouldWatch(true);

    public static final Attribute FROST_DAMAGE = new MKRangedAttribute("attribute.name.mk.frost_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("frost_damage"))
            .setShouldWatch(true);

    public static final Attribute SHADOW_RESISTANCE = new MKRangedAttribute("attribute.name.mk.shadow_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("shadow_resistance"))
            .setShouldWatch(true);

    public static final Attribute SHADOW_DAMAGE = new MKRangedAttribute("attribute.name.mk.shadow_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("shadow_damage"))
            .setShouldWatch(true);

    public static final Attribute HOLY_RESISTANCE = new MKRangedAttribute("attribute.name.mk.holy_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("holy_resistance"))
            .setShouldWatch(true);

    public static final Attribute HOLY_DAMAGE = new MKRangedAttribute("attribute.name.mk.holy_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("holy_damage"))
            .setShouldWatch(true);

    public static final Attribute NATURE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.nature_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("nature_resistance"))
            .setShouldWatch(true);

    public static final Attribute NATURE_DAMAGE = new MKRangedAttribute("attribute.name.mk.nature_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("nature_damage"))
            .setShouldWatch(true);

    public static final Attribute POISON_RESISTANCE = new MKRangedAttribute("attribute.name.mk.poison_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("poison_resistance"))
            .setShouldWatch(true);

    public static final Attribute POISON_DAMAGE = new MKRangedAttribute("attribute.name.mk.poison_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("poison_damage"))
            .setShouldWatch(true);

    public static final Attribute BLEED_RESISTANCE = new MKRangedAttribute("attribute.name.mk.bleed_resistance", 0, -1.0, 1.0)
            .setRegistryName(MKCore.makeRL("bleed_resistance"))
            .setShouldWatch(true);

    public static final Attribute BLEED_DAMAGE = new MKRangedAttribute("attribute.name.mk.bleed_damage", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("bleed_damage"))
            .setShouldWatch(true);

    public static final Attribute ATTACK_REACH = new MKRangedAttribute("attribute.name.mk.attack_reach", 3.0, 0.0, 128)
            .setRegistryName(MKCore.makeRL("attack_reach"))
            .setShouldWatch(true);

    public static final Attribute ABJURATION = new MKRangedAttribute("attribute.name.mk.abjuration", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("abjuration"))
            .setShouldWatch(true);

    public static final Attribute ALTERATON = new MKRangedAttribute("attribute.name.mk.alteration", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("alteration"))
            .setShouldWatch(true);

    public static final Attribute CONJURATION = new MKRangedAttribute("attribute.name.mk.conjuration", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("conjuration"))
            .setShouldWatch(true);

    public static final Attribute DIVINATION = new MKRangedAttribute("attribute.name.mk.divination", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("divination"))
            .setShouldWatch(true);

    public static final Attribute ENCHANTMENT = new MKRangedAttribute("attribute.name.mk.enchantment", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("enchantment"))
            .setShouldWatch(true);

    public static final Attribute PHANTASM = new MKRangedAttribute("attribute.name.mk.phantasm", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("phantasm"))
            .setShouldWatch(true);

    public static final Attribute NECROMANCY = new MKRangedAttribute("attribute.name.mk.necromancy", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("necromancy"))
            .setShouldWatch(true);

    public static final Attribute RESTORATION = new MKRangedAttribute("attribute.name.mk.restoration", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("restoration"))
            .setShouldWatch(true);

    public static final Attribute ARETE = new MKRangedAttribute("attribute.name.mk.arete", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("arete"))
            .setShouldWatch(true);

    public static final Attribute PNEUMA = new MKRangedAttribute("attribute.name.mk.pneuma", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("pneuma"))
            .setShouldWatch(true);

    public static final Attribute PANKRATION = new MKRangedAttribute("attribute.name.mk.pankration", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("pankration"))
            .setShouldWatch(true);

    public static final Attribute EVOCATION = new MKRangedAttribute("attribute.name.mk.evocation", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("evocation"))
            .setShouldWatch(true);

    public static final Attribute MARKSMANSHIP = new MKRangedAttribute("attribute.name.mk.marksmanship", 0, 0, GameConstants.SKILL_MAX)
            .setRegistryName(MKCore.makeRL("marksmanship"))
            .setShouldWatch(true);

    public static final Attribute MAX_POISE = new MKRangedAttribute("attribute.name.mk.max_poise", 0, 0, 2048)
            .setRegistryName(MKCore.makeRL("max_poise"))
            .setShouldWatch(true);

    public static final Attribute POISE_REGEN = new MKRangedAttribute("attribute.name.mk.poise_regen", 2, 0, 2048)
            .setRegistryName(MKCore.makeRL("poise_regen"))
            .setShouldWatch(true);

    public static final Attribute POISE_BREAK_CD = new MKRangedAttribute("attribute.name.mk.poise_break_cd", 20.0, 0, 2048)
            .setRegistryName(MKCore.makeRL("poise_break_cd"))
            .setShouldWatch(true);

    public static final Attribute BLOCK_EFFICIENCY = new MKRangedAttribute("attribute.name.mk.block_efficiency", 0, 0, 1.0)
            .setRegistryName(MKCore.makeRL("block_efficiency"))
            .setShouldWatch(true);

    public static final List<Attribute> SPELL_SKILLS = new ArrayList<>();
    public static final List<Attribute> COMBAT_SKILLS = new ArrayList<>();

    static {
        SPELL_SKILLS.add(EVOCATION);
        SPELL_SKILLS.add(RESTORATION);
        SPELL_SKILLS.add(DIVINATION);
        SPELL_SKILLS.add(ENCHANTMENT);
        SPELL_SKILLS.add(NECROMANCY);
        SPELL_SKILLS.add(PHANTASM);
        SPELL_SKILLS.add(ALTERATON);
        SPELL_SKILLS.add(CONJURATION);
        SPELL_SKILLS.add(ABJURATION);
        COMBAT_SKILLS.add(PNEUMA);
        COMBAT_SKILLS.add(PANKRATION);
        COMBAT_SKILLS.add(ARETE);
        COMBAT_SKILLS.add(MARKSMANSHIP);
    }


    public static void iterateEntityAttributes(Consumer<Attribute> consumer) {
        consumer.accept(COOLDOWN);
        consumer.accept(CASTING_SPEED);
        consumer.accept(HEAL_BONUS);
        consumer.accept(BUFF_DURATION);
        consumer.accept(ATTACK_REACH);
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
        consumer.accept(MARKSMANSHIP);

        consumer.accept(MAX_POISE);
        consumer.accept(POISE_REGEN);
        consumer.accept(POISE_BREAK_CD);
        consumer.accept(BLOCK_EFFICIENCY);
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
