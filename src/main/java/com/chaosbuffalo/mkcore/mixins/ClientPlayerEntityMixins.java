package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixins extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixins(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Nonnull
    @Override
    public Collection<EffectInstance> getActivePotionEffects() {
        List<EffectInstance> fullList = new ArrayList<>(super.getActivePotionEffects());
        MKCore.getPlayer(this).ifPresent(playerData -> {
            playerData.getEffects().effects().forEach(effectInstance -> {
                fullList.add(effectInstance.getClientDisplayEffectInstance());
            });
        });

        return fullList;
    }
}
