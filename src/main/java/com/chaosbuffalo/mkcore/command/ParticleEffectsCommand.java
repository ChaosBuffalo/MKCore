package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.BipedBoneArgument;
import com.chaosbuffalo.mkcore.command.arguments.ParticleAnimationArgument;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class ParticleEffectsCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("pe")
                .then(Commands.literal("addBoneAnimation")
                        .then(Commands.argument("particleAnimation", ParticleAnimationArgument.ParticleAnimation())
                                .then(Commands.argument("bone", BipedBoneArgument.BipedBone())
                                    .executes(ParticleEffectsCommand::addEffectInstance)
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ParticleEffectsCommand::clearEffects)
                );
    }

    static int addEffectInstance(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation particleAnimation = ctx.getArgument("particleAnimation", ResourceLocation.class);
        String bone = StringArgumentType.getString(ctx, "bone");
        BoneEffectInstance inst = new BoneEffectInstance(UUID.randomUUID(), bone, particleAnimation);
        MKCore.getPlayer(player).ifPresent(playerData -> {
            playerData.getAnimationModule().getEffectInstanceTracker().addParticleInstance(inst);
            ChatUtils.sendMessageWithBrackets(player, "Added %s to effect to bone: %s", particleAnimation.toString(), bone);
        });

        return Command.SINGLE_SUCCESS;
    }


    static int clearEffects(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getAnimationModule().getEffectInstanceTracker().clearParticleEffects());

        return Command.SINGLE_SUCCESS;
    }
}