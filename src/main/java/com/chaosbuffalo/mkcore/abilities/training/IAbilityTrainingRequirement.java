package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.util.text.ITextComponent;

public interface IAbilityTrainingRequirement {

    boolean check(IMKEntityData entityData, MKAbility ability);

    void onLearned(IMKEntityData entityData, MKAbility ability);

    ITextComponent describe();


    IAbilityTrainingRequirement NONE = new IAbilityTrainingRequirement() {
        @Override
        public boolean check(IMKEntityData entityData, MKAbility ability) {
            return false;
        }

        @Override
        public void onLearned(IMKEntityData entityData, MKAbility ability) {

        }

        @Override
        public ITextComponent describe() {
            return null;
        }
    };
}
