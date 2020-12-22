package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CritMessagePacket {
    public enum CritType {
        MELEE_CRIT,
        MK_CRIT,
        PROJECTILE_CRIT,
        TYPED_CRIT
    }

    private final int targetId;
    private final UUID sourceUUID;
    private ResourceLocation abilityName;
    private ResourceLocation damageType;
    private final float critDamage;
    private final CritType type;
    private int projectileId;
    private String typeName;

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = CritType.MELEE_CRIT;
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, MKDamageType damageType, String typeName){
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = CritType.TYPED_CRIT;
        this.typeName = typeName;
        this.damageType = damageType.getRegistryName();
    }



    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, ResourceLocation abilityName,
                             MKDamageType damageType) {
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.type = CritType.MK_CRIT;
        this.abilityName = abilityName;
        this.damageType = damageType.getRegistryName();
    }

    public CritMessagePacket(int targetId, UUID sourceUUID, float critDamage, int projectileId) {
        this.type = CritType.PROJECTILE_CRIT;
        this.targetId = targetId;
        this.sourceUUID = sourceUUID;
        this.critDamage = critDamage;
        this.projectileId = projectileId;
    }

    public CritMessagePacket(PacketBuffer pb) {
        this.type = pb.readEnumValue(CritType.class);
        this.targetId = pb.readInt();
        this.sourceUUID = pb.readUniqueId();
        this.critDamage = pb.readFloat();
        if (type == CritType.MK_CRIT) {
            this.abilityName = pb.readResourceLocation();
            this.damageType = pb.readResourceLocation();
        }
        if (type == CritType.PROJECTILE_CRIT) {
            this.projectileId = pb.readInt();
        }
        if (type == CritType.TYPED_CRIT){
            this.damageType = pb.readResourceLocation();
            this.typeName = pb.readString();
        }
    }

    public void toBytes(PacketBuffer pb) {
        pb.writeEnumValue(type);
        pb.writeInt(targetId);
        pb.writeUniqueId(sourceUUID);
        pb.writeFloat(critDamage);
        if (type == CritType.MK_CRIT) {
            pb.writeResourceLocation(this.abilityName);
            pb.writeResourceLocation(this.damageType);
        }
        if (type == CritType.PROJECTILE_CRIT) {
            pb.writeInt(this.projectileId);
        }
        if (type == CritType.TYPED_CRIT){
            pb.writeResourceLocation(damageType);
            pb.writeString(typeName);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean isSelf = player.getUniqueID().equals(sourceUUID);
        PlayerEntity playerSource = player.getEntityWorld().getPlayerByUuid(sourceUUID);
        Entity target = player.getEntityWorld().getEntityByID(targetId);
        if (target == null || playerSource == null) {
            return;
        }
        boolean isSelfTarget = player.getEntityId() == targetId;
        if (isSelf || isSelfTarget) {
            if (!MKConfig.CLIENT.showMyCrits.get()) {
                return;
            }
        } else {
            if (!MKConfig.CLIENT.showOthersCrits.get()) {
                return;
            }
        }
        switch (type) {
            case MELEE_CRIT:
                if (isSelf) {
                    player.sendMessage(new TranslationTextComponent("mkcore.crit.melee.self",
                            target.getDisplayName(),
                            playerSource.getHeldItemMainhand().getDisplayName(),
                            Math.round(critDamage)
                    ).mergeStyle(TextFormatting.DARK_RED), Util.DUMMY_UUID);
                } else {
                    player.sendMessage(new TranslationTextComponent("mkcore.crit.melee.other",
                            playerSource.getDisplayName(),
                            target.getDisplayName(),
                            playerSource.getHeldItemMainhand().getDisplayName(),
                            Math.round(critDamage)
                    ).mergeStyle(TextFormatting.DARK_RED), Util.DUMMY_UUID);
                }
                break;
            case MK_CRIT:
//                messageStyle.setColor(TextFormatting.AQUA);
                MKAbility ability = MKCoreRegistry.getAbility(abilityName);
                MKDamageType mkDamageType = MKCoreRegistry.getDamageType(damageType);
                if (ability == null || mkDamageType == null) {
                    break;
                }
                player.sendMessage(mkDamageType.getAbilityCritMessage(playerSource, (LivingEntity) target, critDamage, ability, isSelf), Util.DUMMY_UUID);
                break;
            case PROJECTILE_CRIT:
                Entity projectile = player.getEntityWorld().getEntityByID(projectileId);
                if (projectile != null) {
                    if (isSelf) {
                        player.sendMessage(new TranslationTextComponent("mkcore.crit.projectile.self",
                                target.getDisplayName(),
                                projectile.getDisplayName(),
                                Math.round(critDamage)
                        ).mergeStyle(TextFormatting.LIGHT_PURPLE), Util.DUMMY_UUID);
                    } else {
                        player.sendMessage(new TranslationTextComponent("mkcore.crit.projectile.other",
                                playerSource.getDisplayName(),
                                target.getDisplayName(),
                                projectile.getDisplayName(),
                                Math.round(critDamage)
                        ).mergeStyle(TextFormatting.LIGHT_PURPLE), Util.DUMMY_UUID);
                    }
                }
                break;
            case TYPED_CRIT:
                mkDamageType = MKCoreRegistry.getDamageType(damageType);
                if (mkDamageType == null) {
                    break;
                }
                player.sendMessage(mkDamageType.getEffectCritMessage(playerSource, (LivingEntity) target, critDamage, typeName, isSelf), Util.DUMMY_UUID);
                break;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }
}