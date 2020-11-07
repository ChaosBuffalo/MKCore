package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetAttackSwingPacket {

    private final int ticksToSet;

    public ResetAttackSwingPacket(int ticksToSet){
        this.ticksToSet = ticksToSet;
    }

    public ResetAttackSwingPacket(PacketBuffer buf) {
        ticksToSet = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(ticksToSet);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        PlayerEntity entity = Minecraft.getInstance().player;
        if (entity == null)
            return;

        MKCore.getPlayer(entity).ifPresent(cap -> {
            cap.getCombatExtension().setTicksSinceSwing(ticksToSet);
        });
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}
