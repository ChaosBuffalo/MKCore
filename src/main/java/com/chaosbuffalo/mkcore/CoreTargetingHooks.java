package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class CoreTargetingHooks {


    private static Targeting.TargetRelation targetHook(Entity source, Entity target) {
        Optional<? extends IMKEntityData> sourceOpt = MKCore.getEntityData(source).resolve();
        Optional<? extends IMKEntityData> targetOpt = MKCore.getEntityData(target).resolve();
        if (sourceOpt.isPresent() && targetOpt.isPresent()) {
            IMKEntityData sourceData = sourceOpt.get();
            IMKEntityData targetData = targetOpt.get();
            if (sourceData.getPets().isPet() || targetData.getPets().isPet()) {
                if (sourceData.getPets().isPet() && targetData.getPets().isPet()) {
                    return Targeting.getTargetRelation(sourceData.getPets().getOwner(), targetData.getPets().getOwner());
                } else if (sourceData.getPets().isPet()) {
                    return Targeting.getTargetRelation(sourceData.getPets().getOwner(), target);
                } else {
                    return Targeting.getTargetRelation(source, targetData.getPets().getOwner());
                }
            }
        }
        return Targeting.TargetRelation.UNHANDLED;
    }

    public static void registerHooks() {
        Targeting.registerRelationCallback(CoreTargetingHooks::targetHook);
    }
}
