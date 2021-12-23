package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.util.text.ITextComponent;

public interface IAbilityTrainingRequirement {

    boolean check(MKPlayerData playerData, MKAbility ability);

    void onLearned(MKPlayerData playerData, MKAbility ability);

    ITextComponent describe(MKPlayerData playerData);


    IAbilityTrainingRequirement NONE = new IAbilityTrainingRequirement() {
        @Override
        public boolean check(MKPlayerData playerData, MKAbility ability) {
            return false;
        }

        @Override
        public void onLearned(MKPlayerData playerData, MKAbility ability) {

        }

        @Override
        public ITextComponent describe(MKPlayerData playerData) {
            return null;
        }
    };
}
