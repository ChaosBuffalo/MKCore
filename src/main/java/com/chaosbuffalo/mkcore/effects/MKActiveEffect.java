package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class MKActiveEffect {

    private final UUID sourceId;
    private final MKEffect effect;
    private final Lazy<EffectInstance> displayEffectInstance = Lazy.of(() -> createDisplayEffectInstance(this));
    private final MKEffectState state;
    private final MKEffectBehaviour behaviour;
    private int stackCount;
    @Nullable
    private ResourceLocation abilityId;

    // Builder
    public MKActiveEffect(MKEffectBuilder<?> builder, MKEffectState state) {
        sourceId = builder.getSourceId();
        effect = builder.getEffect();
        this.behaviour = new MKEffectBehaviour(builder.getBehaviour());
        stackCount = builder.getInitialStackCount();
        this.state = state;
        abilityId = builder.getAbilityId();
    }

    // Deserialize
    public MKActiveEffect(MKEffect effect, UUID sourceId) {
        this.sourceId = sourceId;
        this.effect = effect;
        this.behaviour = new MKEffectBehaviour();
        stackCount = 1;
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

    public void modifyStackCount(int delta) {
        stackCount += delta;
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
        if (abilityId != null) {
            stateTag.putString("abilityId", abilityId.toString());
        }
        return stateTag;
    }

    public void deserializeState(CompoundNBT stateTag) {
        stackCount = stateTag.getInt("stacks");
        behaviour.deserializeState(stateTag.getCompound("behaviour"));
        if (stateTag.contains("abilityId")) {
            abilityId = ResourceLocation.tryCreate(stateTag.getString("abilityId"));
        }
        if (stateTag.contains("state")) {
            state.deserializeStorage(stateTag.getCompound("state"));
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
