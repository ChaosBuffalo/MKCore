package com.chaosbuffalo.mkcore.core;

public enum AbilityType {
    Basic(true),
    Passive(false),
    Ultimate(true),
    Item(true);

    final boolean executable;

    AbilityType(boolean executable) {
        this.executable = executable;
    }

    public boolean isExecutable() {
        return executable;
    }
}
