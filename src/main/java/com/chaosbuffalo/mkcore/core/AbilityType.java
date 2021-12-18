package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;

public enum AbilityType {
    Basic(true, GameConstants.DEFAULT_BASIC_ABILITIES, GameConstants.MAX_BASIC_ABILITIES),
    Passive(false, GameConstants.DEFAULT_PASSIVE_ABILITIES, GameConstants.MAX_PASSIVE_ABILITIES),
    Ultimate(true, GameConstants.DEFAULT_ULTIMATE_ABILITIES, GameConstants.MAX_ULTIMATE_ABILITIES),
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
