package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

import java.util.function.Consumer;

public class MKAttributes {
    // Players Only Attributes
    public static final RangedAttribute MAX_MANA = (RangedAttribute) new RangedAttribute("mk.max_mana", 0, 0, 1024)
//            .setDescription("Max Mana")
            .setShouldWatch(true);

    public static final RangedAttribute MANA_REGEN = (RangedAttribute) new RangedAttribute("mk.mana_regen", 0, 0, 1024)
//            .setDescription("Mana Regen")
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRIT = (RangedAttribute) new MKRangedAttribute("mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
//            .setDescription("Melee Critical Chance")
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_CRIT = (RangedAttribute) new MKRangedAttribute("mk.ranged_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
//            .setDescription("Ranged Critical Chance")
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRIT = (RangedAttribute) new MKRangedAttribute("mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setAdditionIsPercentage(true)
//            .setDescription("Spell Critical Chance")
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("mk.spell_crit_multiplier", 1.5, 0.0, 10.0)
//            .setDescription("Spell Critical Multiplier")
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("mk.melee_crit_multiplier", 0.0, 0.0, 10.0)
//            .setDescription("Melee Critical Multiplier")
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_CRIT_MULTIPLIER = (RangedAttribute) new RangedAttribute("mk.ranged_crit_multiplier", 0.0, 0.0, 10.0)
//            .setDescription("Ranged Critical Multiplier")
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_DAMAGE = (RangedAttribute) new RangedAttribute("mk.ranged_damage", 0.0, 0.0, 2048)
//            .setDescription("Ranged Damage Bonus")
            .setShouldWatch(true);

    public static final RangedAttribute RANGED_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.ranged_resistance", 0, -1.0, 1.0)
//            .setDescription("Ranged Resistance")
            .setShouldWatch(true);

    // Everyone Attributes

    // This is slightly confusing.
    // 1.5 max means the cooldown will progress at most 50% faster than the normal rate. This translates into a 50% reduction in the observed cooldown.
    // 0.25 minimum means that a cooldown can be increased up to 175% of the normal value. This translates into a 75% increase in the observed cooldown
    public static final RangedAttribute COOLDOWN = (RangedAttribute) new RangedAttribute("mk.cooldown_rate", 1, 0.25, 1.5)
//            .setDescription("Cooldown Rate")
            .setShouldWatch(true);

    public static final RangedAttribute HEAL_BONUS = (RangedAttribute) new RangedAttribute("mk.heal_bonus", 1.0, 0.0, 2.0)
//            .setDescription("Heal Bonus Amount")
            .setShouldWatch(true);

    public static final RangedAttribute CASTING_SPEED = (RangedAttribute) new RangedAttribute("mk.casting_speed", 1, 0.25, 1.5)
//            .setDescription("Casting Speed")
            .setShouldWatch(true);

    public static final RangedAttribute BUFF_DURATION = (RangedAttribute) new RangedAttribute("mk.buff_duration", 1.0, 0.0, 5.0)
//            .setDescription("Buff Duration")
            .setShouldWatch(true);

    public static final RangedAttribute ELEMENTAL_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.elemental_resistance", 0, -1.0, 1.0)
//            .setDescription("Elemental Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute ELEMENTAL_DAMAGE = (RangedAttribute) new RangedAttribute("mk.elemental_damage", 0, 0, 2048)
//            .setDescription("Elemental Damage")
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.arcane_resistance", 0, -1.0, 1.0)
//            .setDescription("Arcane Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_DAMAGE = (RangedAttribute) new RangedAttribute("mk.arcane_damage", 0, 0, 2048)
//            .setDescription("Arcane Damage")
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.fire_resistance", 0, -1.0, 1.0)
//            .setDescription("Fire Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_DAMAGE = (RangedAttribute) new RangedAttribute("mk.fire_damage", 0, 0, 2048)
//            .setDescription("Fire Damage")
            .setShouldWatch(true);

    public static final RangedAttribute FROST_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.frost_resistance", 0, -1.0, 1.0)
//            .setDescription("Frost Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute FROST_DAMAGE = (RangedAttribute) new RangedAttribute("mk.frost_damage", 0, 0, 2048)
//            .setDescription("Frost Damage")
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.shadow_resistance", 0, -1.0, 1.0)
//            .setDescription("Shadow Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_DAMAGE = (RangedAttribute) new RangedAttribute("mk.shadow_damage", 0, 0, 2048)
//            .setDescription("Shadow Damage")
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.holy_resistance", 0, -1.0, 1.0)
//            .setDescription("Holy Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_DAMAGE = (RangedAttribute) new RangedAttribute("mk.holy_damage", 0, 0, 2048)
//            .setDescription("Holy Damage")
            .setShouldWatch(true);

    public static final RangedAttribute NATURE_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.nature_resistance", 0, -1.0, 1.0)
//            .setDescription("Nature Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute NATURE_DAMAGE = (RangedAttribute) new RangedAttribute("mk.nature_damage", 0, 0, 2048)
//            .setDescription("Nature Damage")
            .setShouldWatch(true);

    public static final RangedAttribute POISON_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.poison_resistance", 0, -1.0, 1.0)
//            .setDescription("Poison Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute POISON_DAMAGE = (RangedAttribute) new RangedAttribute("mk.poison_damage", 0, 0, 2048)
//            .setDescription("Poison Damage")
            .setShouldWatch(true);

    public static final RangedAttribute BLEED_RESISTANCE = (RangedAttribute) new RangedAttribute("mk.bleed_resistance", 0, -1.0, 1.0)
//            .setDescription("Bleed Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute BLEED_DAMAGE = (RangedAttribute) new RangedAttribute("mk.bleed_damage", 0, 0, 2048)
//            .setDescription("Bleed Damage")
            .setShouldWatch(true);

    public static final RangedAttribute ATTACK_REACH = (RangedAttribute) new RangedAttribute("mk.attack_reach", 3.0, 0.0, 128)
//            .setDescription("Attack Reach")
            .setShouldWatch(true);

    public static void registerEntityAttributes(Consumer<Attribute> attributes) {
        attributes.accept(MKAttributes.COOLDOWN);
        attributes.accept(MKAttributes.CASTING_SPEED);
        attributes.accept(MKAttributes.HEAL_BONUS);
        attributes.accept(MKAttributes.BUFF_DURATION);
        attributes.accept(MKAttributes.ATTACK_REACH);
        for (MKDamageType damageType : MKCoreRegistry.DAMAGE_TYPES.getValues()) {
            damageType.addAttributes(attributes);
        }
    }
}
