package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.handlers.AbilityTalentHandler;
import net.minecraft.util.ResourceLocation;

public class UltimateTalentHandler extends AbilityTalentHandler {
    public UltimateTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
    }
}
