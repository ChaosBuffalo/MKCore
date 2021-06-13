package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalent;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.PassiveTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.SlotCountTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.UltimateTalent;
import com.chaosbuffalo.mkcore.test.abilities.BurningSoul;
import com.chaosbuffalo.mkcore.test.abilities.HealingRain;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class CoreTalents {

    @ObjectHolder("talent.max_health")
    public static AttributeTalent MAX_HEALTH_TALENT;

    @ObjectHolder("talent.armor")
    public static AttributeTalent ARMOR_TALENT;

    @ObjectHolder("talent.movement_speed")
    public static AttributeTalent MOVEMENT_SPEED_TALENT;

    @ObjectHolder("talent.max_mana")
    public static AttributeTalent MAX_MANA_TALENT;

    @ObjectHolder("talent.mana_regen")
    public static AttributeTalent MANA_REGEN_TALENT;

    @ObjectHolder("talent.melee_crit")
    public static AttributeTalent MELEE_CRIT_TALENT;

    @ObjectHolder("talent.spell_crit")
    public static AttributeTalent SPELL_CRIT_TALENT;

    @ObjectHolder("talent.melee_crit_multiplier")
    public static AttributeTalent MELEE_CRIT_MULTIPLIER_TALENT;

    @ObjectHolder("talent.spell_crit_multiplier")
    public static AttributeTalent SPELL_CRIT_MULTIPLIER_TALENT;

    @ObjectHolder("talent.cooldown_reduction")
    public static AttributeTalent COOLDOWN_REDUCTION_TALENT;

    @ObjectHolder("talent.heal_bonus")
    public static AttributeTalent HEAL_BONUS_TALENT;

    @ObjectHolder("talent.attack_damage")
    public static AttributeTalent ATTACK_DAMAGE_TALENT;

    @ObjectHolder("talent.attack_speed")
    public static AttributeTalent ATTACK_SPEED_TALENT;

    @ObjectHolder("talent.ability_slot")
    public static SlotCountTalent ABILITY_SLOT_TALENT;

    @ObjectHolder("talent.passive_ability_slot")
    public static SlotCountTalent PASSIVE_ABILITY_SLOT_TALENT;

    @ObjectHolder("talent.ultimate_ability_slot")
    public static SlotCountTalent ULTIMATE_ABILITY_SLOT_TALENT;


    private static void registerVanillaAttributeTalents(RegistryEvent.Register<MKTalent> event) {
        // Vanilla Attributes
        AttributeTalent maxHealth = new AttributeTalent(
                MKCore.makeRL("talent.max_health"),
                (RangedAttribute) Attributes.MAX_HEALTH,
                UUID.fromString("5d95bcd4-a06e-415a-add0-f1f85e20b18b"))
                .setRequiresStatRefresh(true)
                .setDefaultPerRank(1);
        event.getRegistry().register(maxHealth);

        AttributeTalent armor = new AttributeTalent(
                MKCore.makeRL("talent.armor"),
                (RangedAttribute) Attributes.ARMOR,
                UUID.fromString("1f917d51-efa1-43ee-8af0-b49175c97c0b"))
                .setDefaultPerRank(1);
        event.getRegistry().register(armor);

        AttributeTalent movementSpeed = new AttributeTalent(
                MKCore.makeRL("talent.movement_speed"),
                (RangedAttribute) Attributes.MOVEMENT_SPEED,
                UUID.fromString("95fcf4d0-aaa9-413f-8362-7706e29412f7"))
                .setDisplayAsPercentage(true)
                .setDefaultPerRank(0.01);
        event.getRegistry().register(movementSpeed);

        AttributeTalent attackSpeed = new AttributeTalent(
                MKCore.makeRL("talent.attack_speed"),
                (RangedAttribute) Attributes.ATTACK_SPEED,
                UUID.fromString("e8d4945f-7435-4b1b-990d-3f32815687ff"))
                .setDisplayAsPercentage(true)
                .setOp(AttributeModifier.Operation.MULTIPLY_TOTAL)
                .setDefaultPerRank(0.01);
        event.getRegistry().register(attackSpeed);

        AttributeTalent attackDamage = new AttributeTalent(
                MKCore.makeRL("talent.attack_damage"),
                (RangedAttribute) Attributes.ATTACK_DAMAGE,
                UUID.fromString("752d8f70-a5de-4111-af81-6bd1020b9433"))
                .setOp(AttributeModifier.Operation.ADDITION)
                .setDefaultPerRank(1);
        event.getRegistry().register(attackDamage);
    }

    @SubscribeEvent
    public static void registerTalents(RegistryEvent.Register<MKTalent> event) {
        registerVanillaAttributeTalents(event);

        // MKCore
        registerMKAttributeTalents(event);
        registerPassiveTalents(event);
        registerUltimateTalents(event);
        registerSlotTalents(event);
    }

    private static void registerPassiveTalents(RegistryEvent.Register<MKTalent> event) {
        PassiveTalent burningSoul = new PassiveTalent(
                MKCore.makeRL("talent.burning_soul"),
                BurningSoul.INSTANCE);
        event.getRegistry().register(burningSoul);
    }

    private static void registerUltimateTalents(RegistryEvent.Register<MKTalent> event) {
        UltimateTalent healingRain = new UltimateTalent(
                MKCore.makeRL("talent.healing_rain"),
                HealingRain.INSTANCE);
        event.getRegistry().register(healingRain);
    }

    private static void registerMKAttributeTalents(RegistryEvent.Register<MKTalent> event) {
        AttributeTalent maxMana = new AttributeTalent(
                MKCore.makeRL("talent.max_mana"),
                MKAttributes.MAX_MANA,
                UUID.fromString("50338dba-eaca-4ec8-a71f-13b5924496f4"))
                .setRequiresStatRefresh(true)
                .setDefaultPerRank(1);
        event.getRegistry().register(maxMana);


        AttributeTalent manaRegen = new AttributeTalent(
                MKCore.makeRL("talent.mana_regen"),
                MKAttributes.MANA_REGEN,
                UUID.fromString("87cd1a11-682f-4635-97db-4fedf6a7496b"))
                .setDefaultPerRank(0.5f);
        event.getRegistry().register(manaRegen);

        AttributeTalent meleeCrit = new AttributeTalent(
                MKCore.makeRL("talent.melee_crit"),
                MKAttributes.MELEE_CRIT,
                UUID.fromString("3b9ea27d-61ca-47b4-9bba-e82679b74ddd"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(meleeCrit);

        AttributeTalent spellCrit = new AttributeTalent(
                MKCore.makeRL("talent.spell_crit"),
                MKAttributes.SPELL_CRIT,
                UUID.fromString("9fbc7b94-4836-45ca-933a-4edaabcf2c6a"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(spellCrit);

        AttributeTalent meleeCritDamage = new AttributeTalent(
                MKCore.makeRL("talent.melee_crit_multiplier"),
                MKAttributes.MELEE_CRIT_MULTIPLIER,
                UUID.fromString("0032d49a-ed71-4dfb-a9f5-f0d3dd183e96"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(meleeCritDamage);

        AttributeTalent spellCritDamage = new AttributeTalent(
                MKCore.makeRL("talent.spell_crit_multiplier"),
                MKAttributes.SPELL_CRIT_MULTIPLIER,
                UUID.fromString("a9d6069c-98b9-454d-b59f-c5a6e81966d5"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(spellCritDamage);

        AttributeTalent cooldownRate = new AttributeTalent(
                MKCore.makeRL("talent.cooldown_reduction"),
                MKAttributes.COOLDOWN,
                UUID.fromString("5378ff4c-0606-4781-abc0-c7d3e945b378"))
                .setOp(AttributeModifier.Operation.MULTIPLY_TOTAL)
                .setDisplayAsPercentage(true);
        event.getRegistry().register(cooldownRate);

        AttributeTalent healBonus = new AttributeTalent(
                MKCore.makeRL("talent.heal_bonus"),
                MKAttributes.HEAL_BONUS,
                UUID.fromString("711e57c3-cf2a-4fb5-a503-3dff0a1e007d"));
        event.getRegistry().register(healBonus);
    }

    private static void registerSlotTalents(RegistryEvent.Register<MKTalent> event){
        SlotCountTalent basicSlot = new SlotCountTalent(
                MKCore.makeRL("talent.ability_slot"),
                TalentType.BASIC_SLOT
        );
        event.getRegistry().register(basicSlot);
        SlotCountTalent passiveSlot = new SlotCountTalent(
                MKCore.makeRL("talent.passive_ability_slot"),
                TalentType.PASSIVE_SLOT
        );
        event.getRegistry().register(passiveSlot);
        SlotCountTalent ultimateSlot = new SlotCountTalent(
                MKCore.makeRL("talent.ultimate_ability_slot"),
                TalentType.ULTIMATE_SLOT
        );
        event.getRegistry().register(ultimateSlot);
    }

}
