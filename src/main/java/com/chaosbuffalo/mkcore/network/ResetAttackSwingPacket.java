package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        PlayerEntity entity = Minecraft.getInstance().player;
        if (entity == null)
            return;

        // account for the 2 tick client side cooldown we're experimenting with
        MKCore.getPlayer(entity).ifPresent(cap -> cap.getCombatExtension().setEntityTicksSinceLastSwing(ticksToSet + 2));
        SoundUtils.clientPlaySoundAtPlayer(entity, CoreSounds.attack_cd_reset, entity.getSoundCategory(), 1.0f, 1.0f);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}
