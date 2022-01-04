package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AbilityCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("ability")
                .then(Commands.literal("learn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestUnknownAbilities)
                                .executes(AbilityCommand::learnAbility)))
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestForgettableAbilities)
                                .executes(AbilityCommand::unlearnAbility)))
                .then(Commands.literal("list")
                        .executes(AbilityCommand::listAbilities))
                .then(Commands.literal("pool_size")
                        .then(Commands.argument("size", IntegerArgumentType.integer(GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE))
                                .executes(AbilityCommand::setSlotCount))
                        .executes(AbilityCommand::showSlotCount))
                .then(Commands.literal("pool")
                        .executes(AbilityCommand::showPool))
                ;
    }

    public static CompletableFuture<Suggestions> suggestForgettableAbilities(final CommandContext<CommandSource> context,
                                                                             final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getAbilities()
                                .getKnownStream()
                                .filter(info -> info.getSources().stream().anyMatch(s -> s.getSourceType().isSimple()))
                                .map(MKAbilityInfo::getId)
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }

    static CompletableFuture<Suggestions> suggestUnknownAbilities(final CommandContext<CommandSource> context,
                                                                  final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> MKCoreRegistry.ABILITIES.getKeys().stream()
                                .filter(abilityId -> !playerData.getAbilities().knowsAbility(abilityId))
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }

    static int learnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            MKCore.getPlayer(player).ifPresent(cap -> cap.getAbilities().learnAbility(ability, AbilitySource.ADMIN));
        }

        return Command.SINGLE_SUCCESS;
    }

    static int setSlotCount(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int size = IntegerArgumentType.getInteger(ctx, "size");
        MKCore.getPlayer(player).ifPresent(cap -> cap.getAbilities().setAbilityPoolSize(size));
        return Command.SINGLE_SUCCESS;
    }

    static int showSlotCount(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerAbilityKnowledge abilityKnowledge = cap.getAbilities();
            int currentSize = abilityKnowledge.getCurrentPoolCount();
            int maxSize = abilityKnowledge.getAbilityPoolSize();
            ChatUtils.sendMessageWithBrackets(player, "Ability Pool: %d/%d", currentSize, maxSize);
        });
        return Command.SINGLE_SUCCESS;
    }

    static int showPool(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerAbilityKnowledge abilityKnowledge = cap.getAbilities();
            int currentSize = abilityKnowledge.getCurrentPoolCount();
            int maxSize = abilityKnowledge.getAbilityPoolSize();
            ChatUtils.sendMessageWithBrackets(player, "Ability Pool: %d/%d", currentSize, maxSize);
            abilityKnowledge.getPoolAbilities().forEach(abilityId -> {
                ChatUtils.sendMessageWithBrackets(player, "Pool Ability: %s", abilityId);
            });
        });
        return Command.SINGLE_SUCCESS;
    }

    static int unlearnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
            if (info == null)
                return;
            List<AbilitySource> sources = new ArrayList<>(info.getSources());
            sources.forEach(s -> {
                if (s.getSourceType().isSimple()) {
                    playerData.getAbilities().unlearnAbility(abilityId, s);
                }
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listAbilities(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerAbilityKnowledge abilityKnowledge = cap.getAbilities();
            Collection<MKAbilityInfo> abilities = abilityKnowledge.getAllAbilities();
            if (abilities.size() > 0) {
                ChatUtils.sendMessageWithBrackets(player, "Known Abilities");
                abilities.forEach(info -> {
                    ChatUtils.sendMessageWithBrackets(player, "%s: %b", info.getId(), info.isCurrentlyKnown());
                    if (info.isCurrentlyKnown()) {
                        info.getSources().forEach(s -> ChatUtils.sendMessage(player, "- %s", s.encode()));
                    }
                });
            } else {
                ChatUtils.sendMessageWithBrackets(player, "No known abilities");
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
