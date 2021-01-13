package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.TalentLineIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.TalentTreeIdArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

public class MKCommand {

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("mk")
                .then(StatCommand.register())
                .then(CooldownCommand.register())
                .then(AbilityCommand.register())
                .then(EffectCommand.register())
                .then(PersonaCommand.register())
                .then(TalentCommand.register())
                .then(HotBarCommand.register());
        dispatcher.register(builder);
    }

    public static void registerArguments() {
        ArgumentTypes.register("ability_id", AbilityIdArgument.class, new ArgumentSerializer<>(AbilityIdArgument::ability));
        ArgumentTypes.register("ability_type", HotBarCommand.AbilityTypeArgument.class, new ArgumentSerializer<>(HotBarCommand.AbilityTypeArgument::abilityType));
//        ArgumentTypes.register("talent_id", TalentCommand.TalentIdArgument.class, new ArgumentSerializer<>(TalentCommand.TalentIdArgument::new));
        ArgumentTypes.register("talent_tree_id", TalentTreeIdArgument.class, new ArgumentSerializer<>(TalentTreeIdArgument::talent));
        ArgumentTypes.register("talent_line_id", TalentLineIdArgument.class, new ArgumentSerializer<>(TalentLineIdArgument::talentLine));
    }

}
