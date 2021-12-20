package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

public class UltimateAbilityGroup extends AbilityGroup {

    public UltimateAbilityGroup(MKPlayerData playerData) {
        super(playerData, "ultimate", AbilityGroupId.Ultimate);
    }
}
