package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.arguments.TalentLineIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.TalentTreeIdArgument;
import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class TalentCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("talent")
                .then(Commands.literal("points")
                        .then(Commands.literal("give")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::givePoints)))
                        .then(Commands.literal("take")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(TalentCommand::takePoints)))
                        .executes(TalentCommand::showPoints)
                )
                .then(Commands.literal("learn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::learnTalent))))
                )
                .then(Commands.literal("unlearn")
                        .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes(TalentCommand::unlearnTalent))))
                )
                .then(Commands.literal("tree")
                        .then(Commands.literal("list")
                                .executes(TalentCommand::listTrees))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                        .executes(TalentCommand::unlockTree)))
                        .then(Commands.literal("line")
                                .then(Commands.argument("tree", TalentTreeIdArgument.talentTreeId())
                                        .then(Commands.argument("line", TalentLineIdArgument.talentLine())
                                                .executes(TalentCommand::listLine))))
                )
                .then(Commands.literal("list")
                        .executes(TalentCommand::listTalents)
                );
    }

    static int takePoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.removeTalentPoints(amount)) {
                ChatUtils.sendMessage(player, "Removed %d points", amount);
            } else {
                ChatUtils.sendMessage(player, "Failed to remove %d points", amount);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int givePoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.grantTalentPoints(amount)) {
                ChatUtils.sendMessage(player, "Granted %d points", amount);
            } else {
                ChatUtils.sendMessage(player, "Failed to give %d points", amount);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int showPoints(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            int unspent = talentKnowledge.getUnspentTalentPoints();
            int total = talentKnowledge.getTotalTalentPoints();
            ChatUtils.sendMessage(player, "Talent Points: %d (%d unspent)", total, unspent);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int learnTalent(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.spendTalentPoint(talentId, line, index)) {
                ChatUtils.sendMessage(player, "Spent point in (%s, %s, %d)", talentId, line, index);
            } else {
                ChatUtils.sendMessage(player, "Failed to spend point in (%s, %s, %d)", talentId, line, index);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int unlearnTalent(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");
        int index = IntegerArgumentType.getInteger(ctx, "index");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.refundTalentPoint(talentId, line, index)) {
                ChatUtils.sendMessage(player, "Refund point in (%s, %s, %d)", talentId, line, index);
            } else {
                ChatUtils.sendMessage(player, "Failed to refund point in (%s, %s, %d)", talentId, line, index);
            }

        });

        return Command.SINGLE_SUCCESS;
    }

    static int unlockTree(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();
        ResourceLocation talentId = ctx.getArgument("tree", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.knowsTree(talentId)) {
                ChatUtils.sendMessage(player, "Tree %s already known", talentId);
                return;
            }

            if (talentKnowledge.unlockTree(talentId)) {
                ChatUtils.sendMessage(player, "Tree %s unlocked", talentId);
            } else {
                ChatUtils.sendMessage(player, "Failed to unlock tree %s", talentId);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listTrees(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talents = cap.getTalents();
            Collection<ResourceLocation> knownTalents = talents.getKnownTrees();
            if (knownTalents.size() > 0) {
                ChatUtils.sendMessageWithBrackets(player, "Known Talent Trees");
                knownTalents.forEach(info -> ChatUtils.sendMessage(player, "%s", info));
            } else {
                ChatUtils.sendMessage(player, "You do not know any talent trees");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listLine(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        ResourceLocation treeId = ctx.getArgument("tree", ResourceLocation.class);
        String line = StringArgumentType.getString(ctx, "line");

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();

            TalentTreeDefinition treeDefinition = MKCore.getTalentManager().getTalentTree(treeId);
            if (treeDefinition == null) {
                ChatUtils.sendMessageWithBrackets(player, "Tree %s does not exist", treeId);
                return;
            }

            TalentLineDefinition lineDefinition = treeDefinition.getLine(line);
            if (lineDefinition == null) {
                ChatUtils.sendMessageWithBrackets(player, "Tree %s does not have line %s", treeId, line);
                return;
            }

            ChatUtils.sendMessageWithBrackets(player, "%s - %s", treeId, line);
            lineDefinition.getNodes().stream()
                    .sorted(Comparator.comparing(TalentNode::getPositionString))
                    .forEach(node -> {
                        String msg = describeNode(node, talentKnowledge.getRecord(treeId, line, node.getIndex()));
                        ChatUtils.sendMessage(player, msg);
                    });
        });

        return Command.SINGLE_SUCCESS;
    }

    private static String describeNode(TalentNode node, TalentRecord record) {
        int rank = record != null ? record.getRank() : 0;
        return String.format("%d/%d %s - %s", rank, node.getMaxRanks(), node.getPositionString(), node.getTalent().getRegistryName());
    }

    static int listTalents(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().asPlayer();

        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talents = cap.getTalents();
            Collection<TalentRecord> knownTalents = talents.getKnownTalentsStream()
                    .sorted(Comparator.comparing(r -> r.getNode().getPositionString()))
                    .collect(Collectors.toList());
            if (knownTalents.size() > 0) {
                ChatUtils.sendMessageWithBrackets(player, "Known Talents");
                knownTalents.forEach(info -> {
                    String msg = describeNode(info.getNode(), info);
                    ChatUtils.sendMessage(player, msg);
                });
            } else {
                ChatUtils.sendMessage(player, "No known talents");
            }
        });

        return Command.SINGLE_SUCCESS;
    }


}
