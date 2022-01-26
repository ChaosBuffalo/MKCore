package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.player.PlayerStats;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", PlayerStats::getMana, PlayerStats::setMana))
                .then(createSimpleFloatStat("health", PlayerStats::getHealth, PlayerStats::setHealth))
                .then(createAttributeStat("max_health", Attributes.MAX_HEALTH))
                .then(createAttributeStat("armor", Attributes.ARMOR))
                .then(createAttributeStat("armor_toughness", Attributes.ARMOR_TOUGHNESS))
                .then(createAttributeStat("mana_regen", MKAttributes.MANA_REGEN))
                .then(createAttributeStat("max_mana", MKAttributes.MAX_MANA))
                .then(createAttributeStat("cdr", MKAttributes.COOLDOWN))
                .then(createAttributeStat("melee_crit", MKAttributes.MELEE_CRIT))
                .then(createAttributeStat("melee_crit_multiplier", MKAttributes.MELEE_CRIT_MULTIPLIER))
                .then(createAttributeStat("spell_crit", MKAttributes.SPELL_CRIT))
                .then(createAttributeStat("spell_crit_multiplier", MKAttributes.SPELL_CRIT_MULTIPLIER))
                .then(createAttributeStat("heal_bonus", MKAttributes.HEAL_BONUS))
                .then(createAttributeStat("arcane_damage", MKAttributes.ARCANE_DAMAGE))
                .then(createAttributeStat("arcane_resist", MKAttributes.ARCANE_RESISTANCE))
                .then(createAttributeStat("fire_damage", MKAttributes.FIRE_DAMAGE))
                .then(createAttributeStat("fire_resist", MKAttributes.FIRE_RESISTANCE))
                .then(createAttributeStat("frost_damage", MKAttributes.FROST_DAMAGE))
                .then(createAttributeStat("frost_resist", MKAttributes.FROST_RESISTANCE))
                .then(createAttributeStat("nature_damage", MKAttributes.NATURE_DAMAGE))
                .then(createAttributeStat("nature_resist", MKAttributes.NATURE_RESISTANCE))
                .then(createAttributeStat("holy_damage", MKAttributes.HOLY_DAMAGE))
                .then(createAttributeStat("holy_resist", MKAttributes.HOLY_RESISTANCE))
                .then(createAttributeStat("shadow_damage", MKAttributes.SHADOW_DAMAGE))
                .then(createAttributeStat("shadow_resist", MKAttributes.SHADOW_RESISTANCE))
                .then(createAttributeStat("poison_damage", MKAttributes.POISON_DAMAGE))
                .then(createAttributeStat("poison_resist", MKAttributes.POISON_RESISTANCE))
                .then(createAttributeStat("ranged_damage", MKAttributes.RANGED_DAMAGE))
                .then(createAttributeStat("ranged_crit", MKAttributes.RANGED_CRIT))
                .then(createAttributeStat("ranged_crit_multiplier", MKAttributes.RANGED_CRIT_MULTIPLIER))
                .then(createAttributeStat("ranged_resist", MKAttributes.RANGED_RESISTANCE))
                .then(createAttributeStat("cast_speed", MKAttributes.CASTING_SPEED))
                .then(createAttributeStat("buff_duration", MKAttributes.BUFF_DURATION))
                .then(createAttributeStat("abjuration", MKAttributes.ABJURATION))
                .then(createAttributeStat("alteration", MKAttributes.ALTERATON))
                .then(createAttributeStat("conjuration", MKAttributes.CONJURATION))
                .then(createAttributeStat("divination", MKAttributes.DIVINATION))
                .then(createAttributeStat("enchantment", MKAttributes.ENCHANTMENT))
                .then(createAttributeStat("phantasm", MKAttributes.PHANTASM))
                .then(createAttributeStat("necromancy", MKAttributes.NECROMANCY))
                .then(createAttributeStat("restoration", MKAttributes.RESTORATION))
                .then(createAttributeStat("arete", MKAttributes.ARETE))
                .then(createAttributeStat("pneuma", MKAttributes.PNEUMA))
                .then(createAttributeStat("pankration", MKAttributes.PANKRATION))
                .then(createAttributeStat("evocation", MKAttributes.EVOCATION))
                .then(createAttributeStat("marksmanship", MKAttributes.MARKSMANSHIP))
                .then(createAttributeStat("block_efficiency", MKAttributes.BLOCK_EFFICIENCY))
                .then(createAttributeStat("max_poise", MKAttributes.MAX_POISE))
                .then(createAttributeStat("poise_regen", MKAttributes.POISE_REGEN))
                .then(createAttributeStat("poise_break_cd", MKAttributes.POISE_BREAK_CD))
                .then(createAttributeStat("two_hand_slash", MKAttributes.TWO_HAND_SLASH))
                .then(createAttributeStat("one_hand_slash", MKAttributes.ONE_HAND_SLASH))
                .then(createAttributeStat("one_hand_blunt", MKAttributes.ONE_HAND_BLUNT))
                .then(createAttributeStat("two_hand_blunt", MKAttributes.TWO_HAND_BLUNT))
                .then(createAttributeStat("one_hand_pierce", MKAttributes.ONE_HAND_PIERCE))
                .then(createAttributeStat("two_hand_pierce", MKAttributes.TWO_HAND_PIERCE))
                .then(createAttributeStat("block", MKAttributes.BLOCK))
                ;
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name,
                                                                   Function<PlayerStats, Float> getter,
                                                                   BiConsumer<PlayerStats, Float> setter) {
        ToIntFunction<PlayerEntity> statGet = playerEntity -> {
            MKCore.getPlayer(playerEntity).ifPresent(cap ->
                    ChatUtils.sendMessageWithBrackets(playerEntity, "%s is %f", name, getter.apply(cap.getStats())));

            return Command.SINGLE_SUCCESS;
        };

        ToIntBiFunction<PlayerEntity, Float> statSet;
        if (setter != null) {
            statSet = (playerEntity, value) -> {
                MKCore.getPlayer(playerEntity).ifPresent(cap -> {
                    ChatUtils.sendMessageWithBrackets(playerEntity, "Setting %s to %f", name, value);
                    setter.accept(cap.getStats(), value);
                    ChatUtils.sendMessageWithBrackets(playerEntity, "%s is now %f",
                            name, getter.apply(cap.getStats()));
                });
                return Command.SINGLE_SUCCESS;
            };
        } else {
            statSet = (playerEntity, value) -> {
                ChatUtils.sendMessageWithBrackets(playerEntity, "Setting %s is not supported", name);
                return Command.SINGLE_SUCCESS;
            };
        }

        return createSimpleGetSetIntCommand(name, statGet, statSet);
    }

    static ArgumentBuilder<CommandSource, ?> createAttributeStat(String name, Attribute attribute) {
        ToIntFunction<PlayerEntity> attrGet = playerEntity -> {
            ModifiableAttributeInstance instance = playerEntity.getAttribute(attribute);
            if (instance != null) {
                ChatUtils.sendMessageWithBrackets(playerEntity, "%s is %f (%f base)", name, instance.getValue(), instance.getBaseValue());
            } else {
                ChatUtils.sendMessageWithBrackets(playerEntity, "Attribute %s not found", name);
            }

            return Command.SINGLE_SUCCESS;
        };

        ToIntBiFunction<PlayerEntity, Float> attrSet = (playerEntity, value) -> {
            ModifiableAttributeInstance instance = playerEntity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
                MKCore.getPlayer(playerEntity).ifPresent(x -> x.getSkills().onSetSkill(attribute, value));
                ChatUtils.sendMessageWithBrackets(playerEntity, "%s is now %f (%f base)", name, instance.getValue(), instance.getBaseValue());
            } else {
                ChatUtils.sendMessageWithBrackets(playerEntity, "Attribute %s not found", name);
            }
            return Command.SINGLE_SUCCESS;
        };

        return createAttrGetSetCommand(name, attribute, attrGet, attrSet);
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleGetSetIntCommand(String name,
                                                                          ToIntFunction<PlayerEntity> getterAction,
                                                                          ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(simpleGetAction(getterAction))
                        .then(simpleSetAction(setterAction))
                );
    }

    static ArgumentBuilder<CommandSource, ?> createAttrGetSetCommand(String name, Attribute attr,
                                                                     ToIntFunction<PlayerEntity> getterAction,
                                                                     ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(simpleGetAction(getterAction))
                        .then(simpleSetAction(setterAction))
                        .then(createAttrModCommand(attr))
                );
    }

    @Nonnull
    private static Command<CommandSource> simpleGetAction(ToIntFunction<PlayerEntity> getterAction) {
        return ctx -> getterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player"));
    }

    private static RequiredArgumentBuilder<CommandSource, Float> simpleSetAction(ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("amount", FloatArgumentType.floatArg())
                .requires(StatCommand::hasOpPermission)
                .executes(ctx -> setterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player"),
                        FloatArgumentType.getFloat(ctx, "amount")));
    }

    private static LiteralArgumentBuilder<CommandSource> createAttrModCommand(Attribute attr) {
        return Commands.literal("mod")
                .executes(ctx -> listModifiers(ctx, attr))
                .then(Commands.argument("value", FloatArgumentType.floatArg())
                        .then(Commands.argument("temp", BoolArgumentType.bool())
                                .executes(ctx -> addModifier(ctx, attr,
                                        FloatArgumentType.getFloat(ctx, "value"),
                                        BoolArgumentType.getBool(ctx, "temp")))
                        )
                );
    }

    private static boolean hasOpPermission(CommandSource s) {
        return s.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel());
    }

    static int listModifiers(CommandContext<CommandSource> ctx, Attribute attr) throws CommandSyntaxException {
        PlayerEntity entity = EntityArgument.getPlayer(ctx, "player");

        if (entity.getAttributeManager().hasAttributeInstance(attr)) {
            ModifiableAttributeInstance instance = entity.getAttribute(attr);
            if (instance == null) {
                ChatUtils.sendMessage(entity, "Unable to add modifier - player does not have attribute");
                return Command.SINGLE_SUCCESS;
            }

            ChatUtils.sendMessageWithBrackets(entity, "%s modifiers", attr.getAttributeName());
            for (AttributeModifier mod : instance.getModifierListCopy()) {
                ChatUtils.sendMessage(entity, "%s: %f %s", mod.getName(), mod.getAmount(), mod.getID());
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    static int addModifier(CommandContext<CommandSource> ctx, Attribute attr, float value, boolean temp) throws CommandSyntaxException {
        PlayerEntity entity = EntityArgument.getPlayer(ctx, "player");

        if (entity.getAttributeManager().hasAttributeInstance(attr)) {
            ModifiableAttributeInstance instance = entity.getAttribute(attr);
            if (instance == null) {
                ChatUtils.sendMessage(entity, "Unable to add modifier - player does not have attribute");
                return Command.SINGLE_SUCCESS;
            }

            AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "added by command", value, AttributeModifier.Operation.ADDITION);
            if (temp) {
                instance.applyNonPersistentModifier(mod);
            } else {
                instance.applyPersistentModifier(mod);
            }
            ChatUtils.sendMessage(entity, "Temp mod added with UUID %s", mod.getID());
        }

        return Command.SINGLE_SUCCESS;
    }
}
