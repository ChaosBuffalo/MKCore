package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.command.arguments.*;
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
                .then(HotBarCommand.register())
                .then(ParticleEffectsCommand.register());
        dispatcher.register(builder);
    }

    public static void registerArguments() {
        ArgumentTypes.register("ability_id", AbilityIdArgument.class, new ArgumentSerializer<>(AbilityIdArgument::ability));
        ArgumentTypes.register("ability_group", HotBarCommand.AbilityGroupArgument.class, new ArgumentSerializer<>(HotBarCommand.AbilityGroupArgument::abilityGroup));
        ArgumentTypes.register("talent_id", TalentIdArgument.class, new ArgumentSerializer<>(TalentIdArgument::talentId));
        ArgumentTypes.register("talent_tree_id", TalentTreeIdArgument.class, new ArgumentSerializer<>(TalentTreeIdArgument::talentTreeId));
        ArgumentTypes.register("talent_line_id", TalentLineIdArgument.class, new ArgumentSerializer<>(TalentLineIdArgument::talentLine));
        ArgumentTypes.register("bone_id", BipedBoneArgument.class, new ArgumentSerializer<>(BipedBoneArgument::BipedBone));
        ArgumentTypes.register("particle_animation_id", ParticleAnimationArgument.class, new ArgumentSerializer<>(ParticleAnimationArgument::ParticleAnimation));
        ArgumentTypes.register("ability_source_type_id", AbilitySourceTypeArgument.class, new ArgumentSerializer<>(AbilitySourceTypeArgument::abilitySourceType));
    }

}
