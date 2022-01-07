package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilitiesScreen;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilityPage;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class OpenLearnAbilitiesGuiPacket {
    private final int entityId;
    private final Map<MKAbility, AbilityTrainingEvaluation> abilityOffers;

    public OpenLearnAbilitiesGuiPacket(MKPlayerData playerData, IAbilityTrainer trainingEntity) {
        this.abilityOffers = new HashMap<>();
        entityId = trainingEntity.getEntityId();
        trainingEntity.getTrainableAbilities(playerData).forEach(entry -> {
            AbilityTrainingEvaluation eval = entry.evaluate(playerData);
            abilityOffers.put(eval.getAbility(), eval);
        });
    }

    public OpenLearnAbilitiesGuiPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        int count = buffer.readVarInt();
        this.abilityOffers = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            AbilityTrainingEvaluation offer = AbilityTrainingEvaluation.read(buffer);
            if (offer != null) {
                abilityOffers.put(offer.getAbility(), offer);
            }
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeVarInt(abilityOffers.size());
        abilityOffers.forEach((key, offer) -> offer.write(buffer));
    }

    public static void handle(OpenLearnAbilitiesGuiPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(OpenLearnAbilitiesGuiPacket packet) {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player == null)
                return;
            MKCore.getPlayer(player).ifPresent(playerData -> {
                Minecraft.getInstance().displayGuiScreen(new LearnAbilityPage(playerData, packet.abilityOffers, packet.entityId));
            });

        }
    }
}
