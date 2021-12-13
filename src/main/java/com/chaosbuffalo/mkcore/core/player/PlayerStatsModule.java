package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityStatsModule;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class PlayerStatsModule extends EntityStatsModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("stats");
    private float manaRegenTimer;
    private final SyncFloat mana = new SyncFloat("mana", 0f);

    public PlayerStatsModule(MKPlayerData playerData) {
        super(playerData);
        manaRegenTimer = 0f;
        addSyncPublic(mana);
        addSyncPrivate(abilityTracker);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public float getMeleeCritChance() {
        return (float) getEntity().getAttribute(MKAttributes.MELEE_CRIT).getValue();
    }

    public float getSpellCritChance() {
        return (float) getEntity().getAttribute(MKAttributes.SPELL_CRIT).getValue();
    }

    public float getSpellCritDamage() {
        return (float) getEntity().getAttribute(MKAttributes.SPELL_CRIT_MULTIPLIER).getValue();
    }

    public float getMeleeCritDamage() {
        return (float) getEntity().getAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER).getValue();
    }

    public float getMana() {
        return mana.get();
    }

    public void setMana(float value) {
        setMana(value, true);
    }

    private void setMana(float value, boolean sendUpdate) {
//        MKCore.LOGGER.info("setMana {} {}", value, getMaxMana());
        // Here we're using isAddedToWorld as a proxy to know that attribute deserialization is done and max mana is available
        if (getEntity().isAddedToWorld()) {
            value = MathHelper.clamp(value, 0, getMaxMana());
//            MKCore.LOGGER.info("setMana clamp {}", value);
        }
        mana.set(value, sendUpdate);
    }

    public float getMaxMana() {
        return (float) getEntity().getAttribute(MKAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        getEntity().getAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getManaRegenRate() {
        return (float) getEntity().getAttribute(MKAttributes.MANA_REGEN).getValue();
    }

    public void tick() {
        super.tick();
        updateMana();
    }

    public void onJoinWorld() {
//        MKCore.LOGGER.info("PlayerStats.onJoinWorld");
        if (getEntity().isServerWorld()) {
            setupBaseStats();
        }
    }

    private void addBaseStat(Attribute attribute, double value) {
        LivingEntity entity = getEntity();

        ModifiableAttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
//            MKCore.LOGGER.info("Adding MK base stat {} {} to player", attribute.getAttributeName(), value);
            instance.setBaseValue(value);
        } else {
            MKCore.LOGGER.error("Cannot apply base stat mod to {} - missing attribute {}", getEntity(), attribute);
        }
    }

    private void setupBaseStats() {
        addBaseStat(MKAttributes.MAX_MANA, 20);
        addBaseStat(MKAttributes.MANA_REGEN, 1);
    }

    private void updateMana() {
        if (getManaRegenRate() <= 0.0f) {
            return;
        }

        float max = getMaxMana();
        if (getMana() > max)
            setMana(max);

        if (getMana() == max)
            return;

        manaRegenTimer += 1 / 20f;

        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        while (manaRegenTimer >= i_regen) {
            float current = getMana();
            if (current < max) {
                float newValue = current + 1;
                setMana(newValue, newValue == max);
            }
            manaRegenTimer -= i_regen;
        }
    }

    public void addMana(float value) {
        setMana(getMana() + value);
    }

    public boolean consumeMana(float amount) {
        if (getMana() < amount) {
            return false;
        }

        setMana(getMana() - amount);
        return true;
    }

    public float getAbilityManaCost(MKAbility ability) {
        float manaCost = ability.getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        return getMana() >= getAbilityManaCost(ability);
    }

    public float getTimerPercent(ResourceLocation abilityId, float partialTicks) {
        return abilityTracker.getCooldownPercent(abilityId, partialTicks);
    }

    public void printActiveCooldowns() {
        String msg = "All active cooldowns:";

        getEntity().sendMessage(new StringTextComponent(msg), Util.DUMMY_UUID);
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getMaxCooldownTicks(abilityId);
            ITextComponent line = new StringTextComponent(String.format("%s: %d / %d", name, current, max));
            getEntity().sendMessage(line, Util.DUMMY_UUID);
        });
    }

    public void refreshStats() {
        if (getHealth() > getMaxHealth()) {
            setHealth(MathHelper.clamp(getHealth(), 0, getMaxHealth()));
        }
        if (getMana() > getMaxMana()) {
            setMana(getMana());
        }
    }

    public void onPersonaActivated() {
        refreshStats();
    }

    public void onPersonaDeactivated() {

    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("cooldowns", abilityTracker.serialize());
        tag.putFloat("mana", mana.get());
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        abilityTracker.deserialize(tag.getCompound("cooldowns"));
        if (tag.contains("mana")) {
            setMana(tag.getFloat("mana"));
        }
    }
}
