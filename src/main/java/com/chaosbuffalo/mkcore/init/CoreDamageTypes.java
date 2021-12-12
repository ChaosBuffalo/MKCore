package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.damage.MeleeDamageType;
import com.chaosbuffalo.mkcore.core.damage.RangedDamageType;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreDamageTypes {

    @ObjectHolder("damage.shadow")
    public static MKDamageType ShadowDamage;

    @ObjectHolder("damage.fire")
    public static MKDamageType FireDamage;

    @ObjectHolder("damage.frost")
    public static MKDamageType FrostDamage;

    @ObjectHolder("damage.holy")
    public static MKDamageType HolyDamage;

    @ObjectHolder("damage.poison")
    public static MKDamageType PoisonDamage;

    @ObjectHolder("damage.arcane")
    public static MKDamageType ArcaneDamage;

    @ObjectHolder("damage.bleed")
    public static MKDamageType BleedDamage;

    @ObjectHolder("damage.nature")
    public static MKDamageType NatureDamage;

    @ObjectHolder("damage.melee")
    public static MKDamageType MeleeDamage;

    @ObjectHolder("damage.ranged")
    public static RangedDamageType RangedDamage;


    @SubscribeEvent
    public static void registerDamageTypes(RegistryEvent.Register<MKDamageType> evt) {
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.fire"), MKAttributes.FIRE_DAMAGE,
                MKAttributes.FIRE_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.RED));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.frost"), MKAttributes.FROST_DAMAGE,
                MKAttributes.FROST_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.AQUA));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.holy"), MKAttributes.HOLY_DAMAGE,
                MKAttributes.HOLY_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.GOLD)
                .setCritMultiplier(2.0f));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.poison"), MKAttributes.POISON_DAMAGE,
                MKAttributes.POISON_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.GREEN));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.shadow"), MKAttributes.SHADOW_DAMAGE,
                MKAttributes.SHADOW_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.DARK_PURPLE));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.arcane"), MKAttributes.ARCANE_DAMAGE,
                MKAttributes.ARCANE_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.LIGHT_PURPLE));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.nature"), MKAttributes.NATURE_DAMAGE,
                MKAttributes.NATURE_RESISTANCE, MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                TextFormatting.DARK_GREEN));
        evt.getRegistry().register(new MeleeDamageType(MKCore.makeRL("damage.melee")));
        evt.getRegistry().register(new RangedDamageType(MKCore.makeRL("damage.ranged")));
        evt.getRegistry().register(new MKDamageType(MKCore.makeRL("damage.bleed"), MKAttributes.BLEED_DAMAGE,
                MKAttributes.BLEED_RESISTANCE, MKAttributes.MELEE_CRIT, MKAttributes.MELEE_CRIT_MULTIPLIER,
                TextFormatting.DARK_RED));
    }
}
