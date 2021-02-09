package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.utils.TextUtils;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AbilityCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("ability")
                .then(Commands.literal("learn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestUnknownBasicAbilities)
                                .executes(AbilityCommand::learnAbility)))
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("ability", AbilityIdArgument.ability())
                                .suggests(AbilityCommand::suggestKnownBasicAbilities)
                                .executes(AbilityCommand::unlearnAbility)))
                .then(Commands.literal("list")
                        .executes(AbilityCommand::listAbilities))
                .then(Commands.literal("pool_size")
                        .then(Commands.argument("size", IntegerArgumentType.integer(GameConstants.DEFAULT_ABILITY_POOL_SIZE, GameConstants.MAX_ABILITY_POOL_SIZE))
                                .executes(AbilityCommand::setSlotCount))
                        .executes(AbilityCommand::showSlotCount))
                ;
    }

    public static CompletableFuture<Suggestions> suggestKnownBasicAbilities(final CommandContext<CommandSource> context,
                                                                            final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getKnowledge()
                                .getAbilityKnowledge()
                                .getKnownStream()
                                .filter(info -> info.getAbility().getType().fitsSlot(AbilitySlot.Basic))
                                .map(MKAbilityInfo::getId)
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }

    static CompletableFuture<Suggestions> suggestUnknownBasicAbilities(final CommandContext<CommandSource> context,
                                                                       final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> {
                            Set<MKAbility> allAbilities = new HashSet<>(MKCoreRegistry.ABILITIES.getValues());
                            allAbilities.removeIf(ability -> playerData.getAbilities().knowsAbility(ability.getAbilityId()));
                            allAbilities.removeIf(ability -> ability.getType().getSlotType() != AbilitySlot.Basic);
                            return allAbilities.stream().map(MKAbility::getAbilityId).map(ResourceLocation::toString);
                        })
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
            TextUtils.sendPlayerChatMessage(player, String.format("Ability Pool: %d/%d", currentSize, maxSize));
        });
        return Command.SINGLE_SUCCESS;
    }

    static int unlearnAbility(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation abilityId = ctx.getArgument("ability", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(cap -> cap.getAbilities().unlearnAbility(abilityId));

        return Command.SINGLE_SUCCESS;
    }

    static int listAbilities(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerAbilityKnowledge abilityKnowledge = cap.getAbilities();
            Collection<MKAbilityInfo> abilities = abilityKnowledge.getAllAbilities();
            if (abilities.size() > 0) {
                TextUtils.sendPlayerChatMessage(player, "Known Abilities");
                abilities.forEach(info -> TextUtils.sendPlayerChatMessage(player, String.format("%s: %b", info.getId(), info.isCurrentlyKnown())));
            } else {
                TextUtils.sendPlayerChatMessage(player, "No known abilities");
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
