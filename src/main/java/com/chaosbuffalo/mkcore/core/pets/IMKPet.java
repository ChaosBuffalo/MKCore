package com.chaosbuffalo.mkcore.core.pets;

import net.minecraft.entity.LivingEntity;

public interface IMKPet {

    void addThreat(LivingEntity source, float threatValue);

    void setFollow(LivingEntity toFollower);

}
