package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraftforge.eventbus.api.Cancelable;

public class PlayerAbilityEvent extends PlayerDataEvent {
    private final MKAbilityInfo abilityInfo;

    private PlayerAbilityEvent(MKPlayerData data, MKAbilityInfo abilityInfo) {
        super(data);
        this.abilityInfo = abilityInfo;
    }

    public MKAbility getAbility() {
        return abilityInfo.getAbility();
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public static class Completed extends PlayerAbilityEvent {

        public Completed(MKPlayerData data, MKAbilityInfo abilityInfo) {
            super(data, abilityInfo);
        }
    }

    @Cancelable
    public static class StartCasting extends PlayerAbilityEvent {

        public StartCasting(MKPlayerData data, MKAbilityInfo abilityInfo) {
            super(data, abilityInfo);
        }
    }
}
