package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.sync.SyncInt;

public class PlayerCombatExtensionModule extends CombatExtensionModule implements IPlayerSyncComponentProvider {
    private SyncInt currentProjectileHitCount = new SyncInt("projectileHits", 0);
    private final PlayerSyncComponent sync = new PlayerSyncComponent("combatExtension");

    public PlayerCombatExtensionModule(IMKEntityData entityData) {
        super(entityData);
        addSyncPrivate(currentProjectileHitCount);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public int getCurrentProjectileHitCount(){
        return currentProjectileHitCount.get();
    }

    @Override
    public void setCurrentProjectileHitCount(int currentProjectileHitCount) {
        this.currentProjectileHitCount.set(currentProjectileHitCount);
    }
}
