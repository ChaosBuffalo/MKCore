package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.player.IActiveAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.utils.TextUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class HotBarCommand {

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("hotbar")
                .then(Commands.literal("show")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .executes(HotBarCommand::showActionBar)))
                .then(Commands.literal("set")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                        .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                                .suggests(HotBarCommand::suggestKnownAbilities)
                                                .executes(HotBarCommand::setActionBar)))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                        .executes(HotBarCommand::clearActionBar))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .executes(HotBarCommand::resetActionBar)))
                .then(Commands.literal("add")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                        .suggests(HotBarCommand::suggestKnownAbilities)
                                        .executes(HotBarCommand::addActionBar))))
                .then(Commands.literal("slots")
                        .then(Commands.argument("type", AbilityTypeArgument.abilityType())
                                .then(Commands.argument("count", IntegerArgumentType.integer())
                                        .executes(HotBarCommand::setSlots))))
                ;
    }

    static int setSlots(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        AbilityType type = ctx.getArgument("type", AbilityType.class);
        int count = IntegerArgumentType.getInteger(ctx, "count");

        MKCore.getPlayer(player).ifPresent(playerData -> {
            IActiveAbilityGroup container = playerData.getLoadout().getAbilityGroup(type);
            if (container.setSlots(count)) {
                MKCore.LOGGER.info("Updated slot count for {}", type);
            } else {
                MKCore.LOGGER.error("Failed to update slot count for {}", type);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int setActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        AbilityType type = ctx.getArgument("type", AbilityType.class);
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
            if (abilityKnowledge.knowsAbility(abilityId)) {
                playerData.getLoadout().getAbilityGroup(type).setSlot(slot, abilityId);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int addActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        AbilityType type = ctx.getArgument("type", AbilityType.class);
        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
            if (abilityKnowledge.knowsAbility(abilityId)) {
                int slot = playerData.getLoadout().getAbilityGroup(type).tryEquip(abilityId);
                if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
                    TextUtils.sendChatMessage(player, "No room for ability");
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        AbilityType type = ctx.getArgument("type", AbilityType.class);
        int slot = IntegerArgumentType.getInteger(ctx, "slot");

        MKCore.getPlayer(player).ifPresent(playerData ->
                playerData.getLoadout().getAbilityGroup(type).clearSlot(slot));

        return Command.SINGLE_SUCCESS;
    }

    static int resetActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        AbilityType type = ctx.getArgument("type", AbilityType.class);
        MKCore.getPlayer(player).ifPresent(playerData ->
                playerData.getLoadout().getAbilityGroup(type).resetSlots());

        return Command.SINGLE_SUCCESS;
    }

    static int showActionBar(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        AbilityType type = ctx.getArgument("type", AbilityType.class);
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            IActiveAbilityGroup container = playerData.getLoadout().getAbilityGroup(type);
            int current = container.getCurrentSlotCount();
            int max = container.getMaximumSlotCount();
            TextUtils.sendPlayerChatMessage(player, String.format("%s Action Bar (%d/%d slots)", type, current, max));
            for (int i = 0; i < current; i++) {
                TextUtils.sendChatMessage(player, String.format("%d: %s", i, container.getSlot(i)));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static CompletableFuture<Suggestions> suggestKnownAbilities(final CommandContext<CommandSource> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        AbilityType type = context.getArgument("type", AbilityType.class);
        ServerPlayerEntity player = context.getSource().asPlayer();
        return ISuggestionProvider.suggest(MKCore.getPlayer(player)
                        .map(playerData -> playerData.getAbilities()
                                .getKnownStream()
                                .filter(info -> info.getAbility().getType() == type)
                                .map(MKAbilityInfo::getId)
                                .map(ResourceLocation::toString))
                        .orElse(Stream.empty()),
                builder);
    }

    public static class AbilityTypeArgument implements ArgumentType<AbilityType> {

        public static AbilityTypeArgument abilityType() {
            return new AbilityTypeArgument();
        }

        @Override
        public AbilityType parse(final StringReader reader) throws CommandSyntaxException {
            return AbilityType.valueOf(reader.readString());
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
            return ISuggestionProvider.suggest(Arrays.stream(AbilityType.values()).map(Enum::toString), builder);
        }
    }
}
