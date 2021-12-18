package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;

public enum AbilityType {
    Basic(true, GameConstants.DEFAULT_ACTIVES, GameConstants.MAX_ACTIVES),
    Passive(false, GameConstants.DEFAULT_PASSIVES, GameConstants.MAX_PASSIVES),
    Ultimate(true, GameConstants.DEFAULT_ULTIMATES, GameConstants.MAX_ULTIMATES),
    Item(true, GameConstants.DEFAULT_ITEM_ABILITIES, GameConstants.MAX_ITEM_ABILITIES);

    private final boolean isActive;
    private final int defaultSlots;
    private final int maxSlots;

    AbilityType(boolean executable, int defaultSlots, int maxSlots) {
        this.isActive = executable;
        this.defaultSlots = defaultSlots;
        this.maxSlots = maxSlots;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getDefaultSlots() {
        return defaultSlots;
    }

    public int getMaxSlots() {
        return maxSlots;
    }
}
