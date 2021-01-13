package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TalentIdArgument extends AbilityIdArgument {
    private final TalentType<?> type;

    public TalentIdArgument(TalentType<?> type) {
        this.type = type;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Stream<String> all = MKCoreRegistry.TALENTS.getValues()
                .stream()
                .filter(talent -> talent.getTalentType() == type)
                .map(MKTalent::getTalentId)
                .map(ResourceLocation::toString);

        return ISuggestionProvider.suggest(all, builder);
    }
}
