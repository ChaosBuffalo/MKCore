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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
    private ResourceLocation abilityName;
    private ResourceLocation damageType;
    private final float critDamage;
    private final CritType type;
    private int projectileId;
    private String typeName;
    private final int sourceId;


    public CritMessagePacket(int targetId, int sourceId, float critDamage) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.MELEE_CRIT;
    }

    public CritMessagePacket(int targetId, int sourceId, float critDamage, MKDamageType damageType, String typeName) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.TYPED_CRIT;
        this.typeName = typeName;
        this.damageType = damageType.getRegistryName();
    }


    public CritMessagePacket(int targetId, int sourceId, float critDamage, ResourceLocation abilityName,
                             MKDamageType damageType) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.type = CritType.MK_CRIT;
        this.abilityName = abilityName;
        this.damageType = damageType.getRegistryName();
    }

    public CritMessagePacket(int targetId, int sourceId, float critDamage, int projectileId) {
        this.type = CritType.PROJECTILE_CRIT;
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.critDamage = critDamage;
        this.projectileId = projectileId;
    }

    public CritMessagePacket(PacketBuffer pb) {
        this.type = pb.readEnumValue(CritType.class);
        this.targetId = pb.readInt();
        sourceId = pb.readInt();
        this.critDamage = pb.readFloat();
        if (type == CritType.MK_CRIT) {
            this.abilityName = pb.readResourceLocation();
            this.damageType = pb.readResourceLocation();
        }
        if (type == CritType.PROJECTILE_CRIT) {
            this.projectileId = pb.readInt();
        }
        if (type == CritType.TYPED_CRIT) {
            this.damageType = pb.readResourceLocation();
            this.typeName = pb.readString();
        }
    }

    public void toBytes(PacketBuffer pb) {
        pb.writeEnumValue(type);
        pb.writeInt(targetId);
        pb.writeInt(sourceId);
        pb.writeFloat(critDamage);
        if (type == CritType.MK_CRIT) {
            pb.writeResourceLocation(this.abilityName);
            pb.writeResourceLocation(this.damageType);
        }
        if (type == CritType.PROJECTILE_CRIT) {
            pb.writeInt(this.projectileId);
        }
        if (type == CritType.TYPED_CRIT) {
            pb.writeResourceLocation(damageType);
            pb.writeString(typeName);
        }
    }

    public static void handle(CritMessagePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(CritMessagePacket packet) {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            Entity source =  player.getEntityWorld().getEntityByID(packet.sourceId);
            Entity target = player.getEntityWorld().getEntityByID(packet.targetId);
            if (target == null || source == null) {
                return;
            }
            boolean isSelf = player.isEntityEqual(source);
            boolean isSelfTarget = player.getEntityId() == packet.targetId;
            if (isSelf || isSelfTarget) {
                if (!MKConfig.CLIENT.showMyCrits.get()) {
                    return;
                }
            } else {
                if (!MKConfig.CLIENT.showOthersCrits.get()) {
                    return;
                }
            }
            if (!(source instanceof LivingEntity)){
                return;
            }
            LivingEntity livingSource = (LivingEntity) source;
            switch (packet.type) {
                case MELEE_CRIT:
                    if (isSelf) {
                        player.sendMessage(new TranslationTextComponent("mkcore.crit.melee.self",
                                target.getDisplayName(),
                                livingSource.getHeldItemMainhand().getDisplayName(),
                                Math.round(packet.critDamage)
                        ).mergeStyle(TextFormatting.DARK_RED), Util.DUMMY_UUID);
                    } else {
                        player.sendMessage(new TranslationTextComponent("mkcore.crit.melee.other",
                                livingSource.getDisplayName(),
                                target.getDisplayName(),
                                livingSource.getHeldItemMainhand().getDisplayName(),
                                Math.round(packet.critDamage)
                        ).mergeStyle(TextFormatting.DARK_RED), Util.DUMMY_UUID);
                    }
                    break;
                case MK_CRIT:
//                messageStyle.setColor(TextFormatting.AQUA);
                    MKAbility ability = MKCoreRegistry.getAbility(packet.abilityName);
                    MKDamageType mkDamageType = MKCoreRegistry.getDamageType(packet.damageType);
                    if (ability == null || mkDamageType == null) {
                        break;
                    }
                    player.sendMessage(mkDamageType.getAbilityCritMessage(livingSource, (LivingEntity) target, packet.critDamage, ability, isSelf), Util.DUMMY_UUID);
                    break;
                case PROJECTILE_CRIT:
                    Entity projectile = player.getEntityWorld().getEntityByID(packet.projectileId);
                    if (projectile != null) {
                        if (isSelf) {
                            player.sendMessage(new TranslationTextComponent("mkcore.crit.projectile.self",
                                    target.getDisplayName(),
                                    projectile.getDisplayName(),
                                    Math.round(packet.critDamage)
                            ).mergeStyle(TextFormatting.LIGHT_PURPLE), Util.DUMMY_UUID);
                        } else {
                            player.sendMessage(new TranslationTextComponent("mkcore.crit.projectile.other",
                                    livingSource.getDisplayName(),
                                    target.getDisplayName(),
                                    projectile.getDisplayName(),
                                    Math.round(packet.critDamage)
                            ).mergeStyle(TextFormatting.LIGHT_PURPLE), Util.DUMMY_UUID);
                        }
                    }
                    break;
                case TYPED_CRIT:
                    mkDamageType = MKCoreRegistry.getDamageType(packet.damageType);
                    if (mkDamageType == null) {
                        break;
                    }
                    player.sendMessage(mkDamageType.getEffectCritMessage(livingSource, (LivingEntity) target, packet.critDamage, packet.typeName, isSelf), Util.DUMMY_UUID);
                    break;
            }
        }
    }
}