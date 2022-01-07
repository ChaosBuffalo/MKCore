package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;

import java.util.Collection;
import java.util.UUID;

public class EffectCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("effect")
                .then(Commands.literal("list")
                        .executes(EffectCommand::listEffects)
                )
                .then(Commands.literal("clear")
                        .executes(EffectCommand::clearEffects)
                )
                .then(Commands.literal("test")
                        .executes(EffectCommand::testEffects)
                )
                ;
    }

    static int listEffects(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        Collection<EffectInstance> effects = player.getActivePotionEffects();
        if (effects.size() > 0) {
            ChatUtils.sendMessageWithBrackets(player, "Active MobEffects");
            for (EffectInstance instance : effects) {
                ChatUtils.sendMessage(player, "%s: %d", instance.getPotion().getRegistryName(), instance.getDuration());
            }
        } else {
            ChatUtils.sendMessageWithBrackets(player, "No active MobEffects");
        }
        MKCore.getPlayer(player).ifPresent(playerData -> {
            Collection<MKActiveEffect> mkeffects = playerData.getEffects().effects();
            if (mkeffects.size() > 0) {
                ChatUtils.sendMessageWithBrackets(player, "Active MKEffects");
                for (MKActiveEffect instance : mkeffects) {
                    ChatUtils.sendMessage(player, "%s: %d %d", instance.getEffect().getRegistryName(), instance.getDuration(), instance.getStackCount());
                }
            } else {
                ChatUtils.sendMessageWithBrackets(player, "No active MKEffects");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearEffects(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        player.clearActivePotions();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getEffects().clearEffects());
        ChatUtils.sendMessageWithBrackets(player, "Effects cleared");

        return Command.SINGLE_SUCCESS;
    }

    static int testEffects(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        UUID source = UUID.randomUUID();
//        UUID source = player.getUniqueID();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            MKEffectBuilder<?> newInstance;
//            newInstance = NewHealEffect.INSTANCE.builder(UUID.randomUUID()).configure(1, 2);
//            newInstance = TestFallCountingEffect.INSTANCE.builder(UUID.randomUUID());
//            newInstance = AbilityMagicDamageEffectNew.INSTANCE.builder(player.getUniqueID()).state(s -> {
//                s.base = 1;
//                s.scale = 1;
//            }).periodic(40);
            newInstance = MKAbilityDamageEffect.INSTANCE.builder(source).state(s -> {
                s.damageType = CoreDamageTypes.FireDamage;
                s.setScalingParameters(1, 1);
            }).periodic(20);
            newInstance.timed(200);
//            newInstance.infinite();
            ChatUtils.sendMessage(player, "Adding effect with UUID %s", newInstance.getSourceId());
            playerData.getEffects().addEffect(newInstance);
        });

        return Command.SINGLE_SUCCESS;
    }
}
