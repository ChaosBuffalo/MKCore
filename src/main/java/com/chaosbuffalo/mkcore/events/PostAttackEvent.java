package com.chaosbuffalo.mkcore.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PostAttackEvent extends PlayerEvent {

    public PostAttackEvent(PlayerEntity player) {
        super(player);
    }
}
