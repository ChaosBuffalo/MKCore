package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetAttackSwingPacket {

    private final int ticksToSet;

    public ResetAttackSwingPacket(int ticksToSet) {
        this.ticksToSet = ticksToSet;
    }

    public ResetAttackSwingPacket(PacketBuffer buf) {
        ticksToSet = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(ticksToSet);
    }

    public static void handle(ResetAttackSwingPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(ResetAttackSwingPacket packet) {
            PlayerEntity entity = Minecraft.getInstance().player;
            if (entity == null)
                return;

            MKCore.getPlayer(entity).ifPresent(cap ->
                    cap.getCombatExtension().setEntityTicksSinceLastSwing(packet.ticksToSet));
            SoundUtils.clientPlaySoundAtPlayer(entity, CoreSounds.attack_cd_reset, entity.getSoundCategory(), 1.0f, 1.0f);
        }
    }
}
