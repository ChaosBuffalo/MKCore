package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class PassiveTalent extends AbilityGrantTalent {

    public PassiveTalent(ResourceLocation name, Supplier<? extends MKAbility> ability) {
        super(name, ability, TalentType.PASSIVE);
    }
}
