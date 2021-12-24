package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerLearnAbilityRequestPacket {
    private final int entityId;
    private final List<ResourceLocation> forgetting;
    private final ResourceLocation learning;

    public PlayerLearnAbilityRequestPacket(List<ResourceLocation> forgetting, ResourceLocation learning, int entityId) {
        this.entityId = entityId;
        this.forgetting = forgetting;
        this.learning = learning;
    }

    public PlayerLearnAbilityRequestPacket(ResourceLocation learning, int entityId) {
        this(new ArrayList<>(), learning, entityId);
    }

    public PlayerLearnAbilityRequestPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        learning = buffer.readResourceLocation();
        int count = buffer.readInt();
        forgetting = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forgetting.add(buffer.readResourceLocation());
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(learning);
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


            for (ResourceLocation loc : forgetting) {
                MKAbility ability = MKCoreRegistry.getAbility(loc);
                if (ability == null) {
                    MKCore.LOGGER.error("Forget ability failed because ability with id {} is null for player: {}.", loc.toString(), player);
                    return;
                }
            }
            MKAbility toLearn = MKCoreRegistry.getAbility(learning);
            if (toLearn == null) {
                MKCore.LOGGER.error("Learn ability failed because ability with id {} is null for player: {}.", learning.toString(), player);
            }


            Entity teacher = player.getServerWorld().getEntityByID(entityId);
            if (teacher instanceof IAbilityTrainingEntity) {
                IAbilityTrainer abilityTrainer = ((IAbilityTrainingEntity) teacher).getAbilityTrainer();

                MKCore.getPlayer(player).ifPresent(playerData -> {
                    AbilityTrainingEntry entry = abilityTrainer.getTrainingEntry(toLearn);
                    if (entry == null) {
                        MKCore.LOGGER.error("Trainer {} does not have requested ability {}. Requested by {}", teacher, learning, player);
                        return;
                    }
                    if (!entry.checkRequirements(playerData)) {
                        MKCore.LOGGER.debug("Failed to learn ability {} from {} - unmet requirements", learning, teacher);
                        return;
                    }

                    int count = playerData.getAbilities().getSlotDeficitToLearnAnAbility();
                    if (count != forgetting.size()) {
                        MKCore.LOGGER.debug("Failed to learn ability {} from {} - a", learning, teacher);
                        return;
                    }
                    for (ResourceLocation toForget : forgetting) {
                        if (!playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED)) {
                            MKCore.LOGGER.debug("Failed to learn ability {} from {} - provided unlearned ability for forgetting {}", learning, teacher, toForget);
                            return;
                        }
                    }

                    if (playerData.getAbilities().learnAbility(toLearn, AbilitySource.TRAINED)) {
                        entry.onAbilityLearned(playerData);
                    }
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
