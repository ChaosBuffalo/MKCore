package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.core.AbilityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSlotAbilityPacket {

    private final AbilityType type;
    private final ResourceLocation ability;
    private final int slotIndex;

    public PlayerSlotAbilityPacket(AbilityType type, int slotIndex, ResourceLocation ability) {
        this.type = type;
        this.slotIndex = slotIndex;
        this.ability = ability;
    }


    public PlayerSlotAbilityPacket(PacketBuffer buf) {
        ability = buf.readResourceLocation();
        type = buf.readEnumValue(AbilityType.class);
        slotIndex = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(ability);
        buf.writeEnumValue(type);
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null) {
                return;
            }
            entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(playerData ->
                    playerData.getLoadout()
                            .getAbilityGroup(type)
                            .setSlot(slotIndex, ability));
        });
        ctx.setPacketHandled(true);
    }
}
