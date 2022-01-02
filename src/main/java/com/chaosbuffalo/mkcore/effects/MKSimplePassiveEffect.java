package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;

import java.util.UUID;

public class MKSimplePassiveEffect extends MKEffectInstance {
    public MKSimplePassiveEffect(MKEffect effect, UUID sourceId) {
        super(effect, sourceId);
    }

    @Override
    public boolean performEffect(IMKEntityData entityData, MKActiveEffect instance) {
        return true;
    }

    @Override
    public boolean isReady(IMKEntityData entityData, MKActiveEffect instance) {
        // This is a simple passive meant to provide attributes and does not need to tick
        return false;
    }
}
