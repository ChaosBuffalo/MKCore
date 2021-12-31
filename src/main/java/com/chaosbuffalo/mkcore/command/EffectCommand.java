package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import com.chaosbuffalo.mkcore.effects.status.StunEffectV2;
import com.chaosbuffalo.mkcore.test.v2.NewHealEffect;
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

        MKCore.getPlayer(player).ifPresent(playerData -> {
            MKEffectInstance newInstance;
//            newInstance = NewHealEffect.INSTANCE.createInstance(UUID.randomUUID()).configure(1, 2);
            newInstance = StunEffectV2.INSTANCE.createInstance(UUID.randomUUID());
            newInstance.timed(200);
            ChatUtils.sendMessage(player, "Adding effect with UUID %s", newInstance.getSourceId());
            playerData.getEffects().addEffect(newInstance);
        });

        return Command.SINGLE_SUCCESS;
    }
}
