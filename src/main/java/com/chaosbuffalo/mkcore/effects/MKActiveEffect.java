package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MKActiveEffect {

    private final UUID sourceId;
    private final MKEffect effect;
    private final Lazy<EffectInstance> displayEffectInstance = Lazy.of(() -> createDisplayEffectInstance(this));
    private final MKEffectState state;
    private final MKEffectBehaviour behaviour;
    private int stackCount;
    private float skillLevel;
    @Nullable
    private ResourceLocation abilityId;
    @Nullable
    private LivingEntity sourceEntity;
    @Nullable
    private Entity directEntity;
    @Nullable
    private UUID directUUID;
    private final Map<Attribute, Float> attrSkillLevels = new HashMap<>();

    // Builder
    public MKActiveEffect(MKEffectBuilder<?> builder, MKEffectState state) {
        sourceId = builder.getSourceId();
        effect = builder.getEffect();
        this.behaviour = new MKEffectBehaviour(builder.getBehaviour());
        stackCount = builder.getInitialStackCount();
        skillLevel = builder.getSkillLevel();
        this.state = state;
        abilityId = builder.getAbilityId();
        sourceEntity = builder.getSourceEntity();
        directEntity = builder.getDirectEntity();
        for (Map.Entry<Attribute, MKEffect.Modifier> entry : effect.getAttributeModifierMap().entrySet()){
            if (entry.getValue().skill != null){
                attrSkillLevels.put(entry.getValue().skill, MKAbility.getSkillLevel(sourceEntity, entry.getValue().skill));
            }
        }
        if (directEntity != null) {
            directUUID = directEntity.getUniqueID();
        }
    }

    // Deserialize
    public MKActiveEffect(MKEffect effect, UUID sourceId) {
        this.sourceId = sourceId;
        this.effect = effect;
        this.behaviour = new MKEffectBehaviour();
        stackCount = 1;
        skillLevel = 0.0f;
        this.state = effect.makeState();
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public MKEffectState getState() {
        return state;
    }

    public <T extends MKEffectState> T getState(@SuppressWarnings("unused") TypeToken<T> typeBound) {
        return (T) getState();
    }

    public ResourceLocation getAbilityId() {
        if (abilityId == null)
            return MKCoreRegistry.INVALID_ABILITY;
        return abilityId;
    }

    public MKEffect getEffect() {
        return effect;
    }

    public MKEffectBehaviour getBehaviour() {
        return behaviour;
    }

    public int getDuration() {
        return behaviour.getDuration();
    }

    public void setDuration(int duration) {
        behaviour.setDuration(duration);
    }

    public void modifyDuration(int delta) {
        behaviour.modifyDuration(delta);
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int count) {
        stackCount = count;
    }

    public void setSkillLevel(float skillLevel) {
        this.skillLevel = skillLevel;
    }

    public float getSkillLevel() {
        return skillLevel;
    }

    public void modifyStackCount(int delta) {
        stackCount += delta;
    }

    @Nullable
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }

    public boolean hasSourceEntity() {
        return getSourceEntity() != null;
    }

    @Nullable
    public Entity getDirectEntity() {
        if (directEntity == null)
            return sourceEntity;
        return directEntity;
    }

    public boolean hasDirectEntity() {
        return getDirectEntity() != null;
    }

    public void recoverState(IMKEntityData targetData) {
        Entity rawSource = findEntity(sourceEntity, getSourceId(), targetData);
        if (rawSource instanceof LivingEntity) {
            sourceEntity = (LivingEntity) rawSource;
        }

        if (directEntity == null && directUUID != null) {
            directEntity = findEntity(directEntity, directUUID, targetData);
        }
    }

    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, IMKEntityData targetData) {
        if (entity != null)
            return entity;
        World world = targetData.getEntity().getEntityWorld();
        if (!world.isRemote()) {
            return ((ServerWorld) world).getEntityByUuid(entityId);
        }
        return null;
    }

    public CompoundNBT serializeClient() {
        CompoundNBT stateTag = new CompoundNBT();
        stateTag.put("state", serializeState());
        return stateTag;
    }

    public static MKActiveEffect deserializeClient(ResourceLocation effectId, UUID sourceId, CompoundNBT tag) {
        MKEffect effect = MKCoreRegistry.EFFECTS.getValue(effectId);
        if (effect == null) {
            return null;
        }

        MKActiveEffect active = effect.createInstance(sourceId);
        active.deserializeState(tag.getCompound("state"));
        return active;
    }

    public CompoundNBT serializeState() {
        CompoundNBT stateTag = new CompoundNBT();
        stateTag.put("behaviour", behaviour.serialize());
        stateTag.putInt("stacks", getStackCount());
        stateTag.putFloat("skillLevel", getSkillLevel());
        if (abilityId != null) {
            stateTag.putString("abilityId", abilityId.toString());
        }
        if (directUUID != null) {
            stateTag.putUniqueId("directEntity", directUUID);
        }
        if (!attrSkillLevels.isEmpty()){
            CompoundNBT attrTag = new CompoundNBT();
            for (Map.Entry<Attribute, Float> entry : attrSkillLevels.entrySet()){
                attrTag.putFloat(entry.getKey().getRegistryName().toString(), entry.getValue());
            }
            stateTag.put("attrSkills", attrTag);
        }


        return stateTag;
    }

    public float getAttrSkillLevel(Attribute skill){
        return attrSkillLevels.getOrDefault(skill, 0f);
    }

    public void deserializeState(CompoundNBT stateTag) {
        stackCount = stateTag.getInt("stacks");
        skillLevel = stateTag.getFloat("skillLevel");
        behaviour.deserializeState(stateTag.getCompound("behaviour"));
        if (stateTag.contains("abilityId")) {
            abilityId = ResourceLocation.tryCreate(stateTag.getString("abilityId"));
        }
        if (stateTag.contains("state")) {
            state.deserializeStorage(stateTag.getCompound("state"));
        }
        if (stateTag.contains("directEntity")) {
            directUUID = stateTag.getUniqueId("directEntity");
        }
        if (stateTag.contains("attrSkills")){
            CompoundNBT attrTag = stateTag.getCompound("attrSkills");
            for (String key : attrTag.keySet()){
                ResourceLocation attrLoc = new ResourceLocation(key);
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrLoc);
                if (attribute != null){
                    float val = attrTag.getFloat(key);
                    attrSkillLevels.put(attribute, val);
                }
            }
        }
    }

    public CompoundNBT serializeStorage() {
        CompoundNBT tag = serializeState();
        serializeId(tag);
        CompoundNBT stateTag = new CompoundNBT();
        state.serializeStorage(stateTag);
        if (!stateTag.isEmpty()) {
            tag.put("state", stateTag);
        }
        return tag;
    }

    public static MKActiveEffect deserializeStorage(UUID sourceId, CompoundNBT tag) {
        ResourceLocation effectId = deserializeId(tag);

        MKEffect effect = MKCoreRegistry.EFFECTS.getValue(effectId);
        if (effect == null) {
            return null;
        }

        MKActiveEffect active = effect.createInstance(sourceId);
        active.deserializeState(tag);
        if (!active.getState().validateOnLoad(active)) {
            MKCore.LOGGER.warn("Effect {} failed load validation", active);
            return null;
        }
        return active;
    }

    private void serializeId(CompoundNBT nbt) {
        MKNBTUtil.writeResourceLocation(nbt, "effectId", effect.getId());
    }

    private static ResourceLocation deserializeId(CompoundNBT tag) {
        return MKNBTUtil.readResourceLocation(tag, "effectId");
    }

    @Override
    public String toString() {
        return "MKActiveEffect{" +
                "sourceId=" + sourceId +
                ", effect=" + effect +
                ", behaviour=" + behaviour +
                ", stackCount=" + stackCount +
                ", abilityId=" + getAbilityId() +
                ", skillLevel=" + skillLevel +
                '}';
    }

    // Only called for the local player on the client
    public EffectInstance getClientDisplayEffectInstance() {
        return displayEffectInstance.get();
    }

    private static EffectInstance createDisplayEffectInstance(MKActiveEffect effectInstance) {
        return new EffectInstance(effectInstance.getEffect().getVanillaWrapper()) {

            @Override
            public boolean getIsPotionDurationMax() {
                return effectInstance.getBehaviour().isInfinite();
            }

            @Override
            public int getDuration() {
                // Even though we override getIsPotionDurationMax we still need a large number so the
                // in-game GUI doesn't flash continuously
                if (getIsPotionDurationMax())
                    return Integer.MAX_VALUE;
                return effectInstance.getBehaviour().getDuration();
            }

            @Override
            public int getAmplifier() {
                // "Amplifier" in vanilla is the number of ranks above 1
                return Math.max(0, effectInstance.getStackCount() - 1);
            }

            @Override
            public void renderHUDEffect(AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
                super.renderHUDEffect(gui, mStack, x, y, z, alpha);
            }

            @Override
            public boolean shouldRender() {
                return effectInstance.getEffect().shouldRender(effectInstance);
            }

            @Override
            public boolean shouldRenderHUD() {
                return effectInstance.getEffect().shouldRenderHUD(effectInstance);
            }

            @Override
            public boolean shouldRenderInvText() {
                return effectInstance.getEffect().shouldRenderInvText(effectInstance);
            }
        };
    }

}
