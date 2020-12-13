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

    public static void registerEntityAttributes(Consumer<Attribute> attributes) {
//        MKCore.LOGGER.info("Adding entity attributes");
        attributes.accept(COOLDOWN);
        attributes.accept(CASTING_SPEED);
        attributes.accept(HEAL_BONUS);
        attributes.accept(BUFF_DURATION);
        attributes.accept(ATTACK_REACH);
        attributes.accept(ELEMENTAL_DAMAGE);
        attributes.accept(ELEMENTAL_RESISTANCE);
        attributes.accept(ARCANE_DAMAGE);
        attributes.accept(ARCANE_RESISTANCE);
        attributes.accept(FIRE_DAMAGE);
        attributes.accept(FIRE_RESISTANCE);
        attributes.accept(FROST_DAMAGE);
        attributes.accept(FROST_RESISTANCE);
        attributes.accept(SHADOW_DAMAGE);
        attributes.accept(SHADOW_RESISTANCE);
        attributes.accept(HOLY_DAMAGE);
        attributes.accept(HOLY_RESISTANCE);
        attributes.accept(NATURE_DAMAGE);
        attributes.accept(NATURE_RESISTANCE);
        attributes.accept(POISON_DAMAGE);
        attributes.accept(POISON_RESISTANCE);
        attributes.accept(BLEED_DAMAGE);
        attributes.accept(BLEED_RESISTANCE);
    }

    public static void registerPlayerAttributes(Consumer<Attribute> attributes) {
//        MKCore.LOGGER.info("Adding player attributes");
        attributes.accept(MAX_MANA);
        attributes.accept(MANA_REGEN);
        attributes.accept(MELEE_CRIT);
        attributes.accept(MELEE_CRIT_MULTIPLIER);
        attributes.accept(SPELL_CRIT);
        attributes.accept(SPELL_CRIT_MULTIPLIER);

        attributes.accept(RANGED_CRIT);
        attributes.accept(RANGED_CRIT_MULTIPLIER);
        attributes.accept(RANGED_DAMAGE);
        attributes.accept(RANGED_RESISTANCE);
    }

    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {

        @SubscribeEvent
        public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
            MKCore.LOGGER.info("MKCORE REGISTER ATTRIBUTES");
            registerEntityAttributes(event.getRegistry()::register);
            registerPlayerAttributes(event.getRegistry()::register);
        }
    }
}
