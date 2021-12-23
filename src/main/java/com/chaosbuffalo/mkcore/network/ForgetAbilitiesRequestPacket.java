package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ForgetAbilitiesRequestPacket {
    private final List<ResourceLocation> forgetting;

    public ForgetAbilitiesRequestPacket(List<ResourceLocation> forgetting) {
        this.forgetting = forgetting;
    }

    public ForgetAbilitiesRequestPacket(PacketBuffer buffer) {
        int count = buffer.readInt();
        forgetting = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forgetting.add(buffer.readResourceLocation());
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(forgetting.size());
        for (ResourceLocation loc : forgetting) {
            buffer.writeResourceLocation(loc);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player == null)
                return;

            MKCore.getPlayer(player).ifPresent(playerData -> {
                for (ResourceLocation toForget : forgetting) {
                    playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED);
                }
            });


        });
        ctx.setPacketHandled(true);
    }
}
