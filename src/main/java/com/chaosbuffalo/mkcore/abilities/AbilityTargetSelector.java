package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class AbilityTargetSelector {

    private final BiFunction<IMKEntityData, MKAbility, AbilityContext> selector;
    private Set<MemoryModuleType<?>> requiredMemories;
    private String descriptionKey;
    private final List<BiFunction<MKAbility, IMKEntityData, ITextComponent>> additionalDescriptors;
    private boolean showTargetType;

    public AbilityTargetSelector(BiFunction<IMKEntityData, MKAbility, AbilityContext> selector) {
        this.selector = selector;
        this.additionalDescriptors = new ArrayList<>();
        this.showTargetType = true;
        descriptionKey = "";
    }

    public AbilityTargetSelector setDescriptionKey(String description) {
        this.descriptionKey = description;
        return this;
    }

    public AbilityTargetSelector setShowTargetType(boolean showTargetType) {
        this.showTargetType = showTargetType;
        return this;
    }

    public AbilityTargetSelector addDynamicDescription(BiFunction<MKAbility, IMKEntityData, ITextComponent> description) {
        additionalDescriptors.add(description);
        return this;
    }

    public boolean doShowTargetType() {
        return showTargetType;
    }

    public void buildDescription(MKAbility ability, IMKEntityData casterData, Consumer<ITextComponent> consumer) {
        consumer.accept(getDescriptionWithHeading(ability));
        additionalDescriptors.forEach(func -> consumer.accept(func.apply(ability, casterData)));
    }

    private IFormattableTextComponent getDescriptionWithHeading(MKAbility ability) {
        if (showTargetType) {
            ITextComponent type = ability.getTargetContext().getLocalizedDescription();
            return new TranslationTextComponent("mkcore.ability_description.target_with_type", getDescription(), type);
        } else {
            return new TranslationTextComponent("mkcore.ability_description.target", getDescription());
        }
    }

    public ITextComponent getDescription() {
        return new TranslationTextComponent(descriptionKey);
    }

    public AbilityTargetSelector setRequiredMemories(Set<MemoryModuleType<?>> types) {
        requiredMemories = types;
        return this;
    }

    public Set<MemoryModuleType<?>> getRequiredMemories() {
        if (requiredMemories != null) {
            return Collections.unmodifiableSet(requiredMemories);
        } else {
            return Collections.emptySet();
        }
    }

    public BiFunction<IMKEntityData, MKAbility, AbilityContext> getSelector() {
        return selector;
    }

    public AbilityContext createContext(IMKEntityData casterData, MKAbility ability) {
        return selector.apply(casterData, ability);
    }

    public boolean validateContext(IMKEntityData casterData, AbilityContext context) {
        return requiredMemories == null || requiredMemories.stream().allMatch(context::hasMemory);
    }
}
