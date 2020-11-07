package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;

public class CombatExtensionModule {
    private static final int COMBAT_TIMEOUT = GameConstants.TICKS_PER_SECOND * 8;

    private int ticksSinceSwing;
    private int currentSwingCount;
    private boolean midCombo;
    private IMKEntityData entityData;

    public CombatExtensionModule(IMKEntityData entityData){
        this.entityData = entityData;
        ticksSinceSwing = 0;
        currentSwingCount = 0;
        midCombo = false;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public void tick(){
        ticksSinceSwing++;
        if (midCombo && ticksSinceSwing >= COMBAT_TIMEOUT){
            currentSwingCount = 0;
            midCombo = false;
        }
    }

    public void setTicksSinceSwing(int newTicks){
        getEntityData().getEntity().ticksSinceLastSwing = newTicks;
    }

    public void recordSwing(){
        ticksSinceSwing = 0;
        currentSwingCount++;
        midCombo = true;
    }

    public boolean isMidCombo() {
        return midCombo;
    }

    public int getCurrentSwingCount() {
        return currentSwingCount;
    }
}
