package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.player.PlayerStatsModule;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

public class StatCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("stat")
                .then(createSimpleFloatStat("mana", PlayerStatsModule::getMana, PlayerStatsModule::setMana))
                .then(createSimpleFloatStat("health", PlayerStatsModule::getHealth, PlayerStatsModule::setHealth))
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
                .then(createAttributeStat("elemental_damage", MKAttributes.ELEMENTAL_DAMAGE))
                .then(createAttributeStat("elemental_resist", MKAttributes.ELEMENTAL_RESISTANCE))
                .then(createAttributeStat("ranged_damage", MKAttributes.RANGED_DAMAGE))
                .then(createAttributeStat("ranged_crit", MKAttributes.RANGED_CRIT))
                .then(createAttributeStat("ranged_crit_multiplier", MKAttributes.RANGED_CRIT_MULTIPLIER))
                .then(createAttributeStat("ranged_resist", MKAttributes.RANGED_RESISTANCE))
                .then(createAttributeStat("cast_speed", MKAttributes.CASTING_SPEED))
                .then(createAttributeStat("buff_duration", MKAttributes.BUFF_DURATION))
                ;
    }

    static ArgumentBuilder<CommandSource, ?> createSimpleFloatStat(String name, Function<PlayerStatsModule, Float> getter, BiConsumer<PlayerStatsModule, Float> setter) {
        ToIntFunction<PlayerEntity> getAction = playerEntity -> {
            playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is %f", name, getter.apply(cap.getStats()))));

            return Command.SINGLE_SUCCESS;
        };

        ToIntBiFunction<PlayerEntity, Float> setAction;
        if (setter != null) {
            setAction = (playerEntity, value) -> {
                playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("Setting %s to %f", name, value));
                    setter.accept(cap.getStats(), value);
                    TextUtils.sendPlayerChatMessage(playerEntity, String.format("%s is now %f",
                            name, getter.apply(cap.getStats())));
                });
                return Command.SINGLE_SUCCESS;
            };
        } else {
            setAction = (playerEntity, value) -> {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Setting %s is not supported", name));
                return Command.SINGLE_SUCCESS;
            };
        }

        return createSimple(name, getAction, setAction);
    }

    static ArgumentBuilder<CommandSource, ?> createAttributeStat(String name, Attribute attribute) {
        ToIntFunction<PlayerEntity> getAction = playerEntity -> {
            ModifiableAttributeInstance instance = playerEntity.getAttribute(attribute);
            //noinspection ConstantConditions
            if (instance != null) {
                String value = String.format("%s is %f (%f base)", name, instance.getValue(), instance.getBaseValue());
                TextUtils.sendPlayerChatMessage(playerEntity, value);
            } else {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Attribute %s not found", name));
            }

            return Command.SINGLE_SUCCESS;
        };

        ToIntBiFunction<PlayerEntity, Float> setAction = (playerEntity, value) -> {
            ModifiableAttributeInstance instance = playerEntity.getAttribute(attribute);
            //noinspection ConstantConditions
            if (instance != null) {
                instance.setBaseValue(value);
                String output = String.format("%s is now %f (%f base)", name, instance.getValue(), instance.getBaseValue());
                TextUtils.sendPlayerChatMessage(playerEntity, output);
            } else {
                TextUtils.sendPlayerChatMessage(playerEntity, String.format("Attribute %s not found", name));
            }
            return Command.SINGLE_SUCCESS;
        };

        return createAttrCore(name, attribute, getAction, setAction);
    }

    static ArgumentBuilder<CommandSource, ?> createSimple(String name, ToIntFunction<PlayerEntity> getterAction, ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> getterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player")))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .requires(s -> s.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()))
                                .executes(ctx -> setterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player"),
                                        FloatArgumentType.getFloat(ctx, "amount")))));
    }

    static ArgumentBuilder<CommandSource, ?> createAttrCore(String name, Attribute attr, ToIntFunction<PlayerEntity> getterAction, ToIntBiFunction<PlayerEntity, Float> setterAction) {
        return Commands.argument("player", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> getterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player")))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .requires(s -> s.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()))
                                .executes(ctx -> setterAction.applyAsInt(EntityArgument.getPlayer(ctx, "player"),
                                        FloatArgumentType.getFloat(ctx, "amount"))))
                        .then(Commands.literal("mod")
                                .executes(ctx -> listModifiers(ctx, attr))
                                .then(Commands.argument("value", FloatArgumentType.floatArg())
                                        .then(Commands.argument("temp", BoolArgumentType.bool())
                                                .executes(ctx -> addModifier(ctx, attr, FloatArgumentType.getFloat(ctx, "value"), BoolArgumentType.getBool(ctx, "temp")))))));
    }

    static int listModifiers(CommandContext<CommandSource> ctx, Attribute attr) throws CommandSyntaxException {
        PlayerEntity entity = EntityArgument.getPlayer(ctx, "player");

        if (entity.getAttributeManager().hasAttributeInstance(attr)) {
            ModifiableAttributeInstance instance = entity.getAttribute(attr);
            if (instance == null) {
                TextUtils.sendChatMessage(entity, "Unable to add modifier - player does not have attribute");
                return Command.SINGLE_SUCCESS;
            }

            TextUtils.sendPlayerChatMessage(entity, String.format("%s modifiers", attr.getAttributeName()));
            for (AttributeModifier mod : instance.getModifierListCopy()) {
                String msg = String.format("%s: %f %s", mod.getName(), mod.getAmount(), mod.getID().toString());
                TextUtils.sendChatMessage(entity, msg);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    static int addModifier(CommandContext<CommandSource> ctx, Attribute attr, float value, boolean temp) throws CommandSyntaxException {
        PlayerEntity entity = EntityArgument.getPlayer(ctx, "player");

        if (entity.getAttributeManager().hasAttributeInstance(attr)) {
            ModifiableAttributeInstance instance = entity.getAttribute(attr);
            if (instance == null) {
                TextUtils.sendChatMessage(entity, "Unable to add modifier - player does not have attribute");
                return Command.SINGLE_SUCCESS;
            }

            AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "added by command", value, AttributeModifier.Operation.ADDITION);
            if (temp) {
                instance.applyNonPersistentModifier(mod);
            } else {
                instance.applyPersistentModifier(mod);
            }
            TextUtils.sendChatMessage(entity, String.format("Temp mod added with UUID %s", mod.getID().toString()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
