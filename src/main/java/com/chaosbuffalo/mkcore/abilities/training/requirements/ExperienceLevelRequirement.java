package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ExperienceLevelRequirement implements IAbilityTrainingRequirement {
    private final int requiredLevel;

    public ExperienceLevelRequirement(int reqLevel) {
        requiredLevel = reqLevel;
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        PlayerEntity playerEntity = playerData.getEntity();
        return playerEntity.experienceLevel >= requiredLevel;
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {
        PlayerEntity playerEntity = playerData.getEntity();
        playerEntity.addExperienceLevel(-requiredLevel);
    }

    @Override
    public ITextComponent describe(MKPlayerData playerData) {
        return new StringTextComponent(String.format("You must be at least level %d", requiredLevel));
    }
}
