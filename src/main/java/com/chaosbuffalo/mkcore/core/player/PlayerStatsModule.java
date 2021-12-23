package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class PlayerStatsModule extends EntityStatsModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("stats");
    private float manaRegenTimer;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final SyncFloat poise = new SyncFloat("poise", 0f);
    public static final ResourceLocation POISE_BREAK_TIMER = new ResourceLocation(MKCore.MOD_ID, "timer.poise_break");
//    private final SyncFloat poise_break = new SyncFloat("poise_break", 0f);

    public PlayerStatsModule(MKPlayerData playerData) {
        super(playerData);
        manaRegenTimer = 0f;
        addSyncPublic(mana);
        addSyncPrivate(poise);
//        addSyncPrivate(poise_break);
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

    public float getMaxPoise() {
        return (float) getEntity().getAttribute(MKAttributes.MAX_POISE).getValue();
    }

    public void setMaxPoise(float max){
        getEntity().getAttribute(MKAttributes.MAX_POISE).setBaseValue(max);
        setPoise(getPoise()); // Refresh the poise to account for the updated maximum
    }

    public void setPoise(float value){
        setPoise(value, true);
    }

    private void setPoise(float value, boolean sendUpdate) {
        if (getEntity().isAddedToWorld()) {
            value = MathHelper.clamp(value, 0, getMaxPoise());
//            MKCore.LOGGER.info("setMana clamp {}", value);
        }
        poise.set(value, sendUpdate);
    }

    public float getMaxMana() {
        return (float) getEntity().getAttribute(MKAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        getEntity().getAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getPoiseRegenRate(){
        return (float) getEntity().getAttribute(MKAttributes.POISE_REGEN).getValue();
    }

    public float getManaRegenRate() {
        return (float) getEntity().getAttribute(MKAttributes.MANA_REGEN).getValue();
    }

    public void tick() {
        super.tick();
        updateMana();
        updatePoise();
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

    public float getPoise(){
        return poise.get();
    }

    public int getPoiseBreakTime(){
        return getTimer(POISE_BREAK_TIMER);
    }

    private void setupBaseStats() {
        addBaseStat(MKAttributes.MAX_MANA, 20);
        addBaseStat(MKAttributes.MANA_REGEN, 1);

    }

    public float getPoiseBreakCooldown(){
        return (float) getEntity().getAttribute(MKAttributes.POISE_BREAK_CD).getValue();
    }



    public void breakPoise(){
        setPoise(0);
        setTimer(POISE_BREAK_TIMER, Math.round(getPoiseBreakCooldown() * GameConstants.TICKS_PER_SECOND));
        getEntity().stopActiveHand();
    }

    public boolean isPoiseBroke(){
        return getTimer(POISE_BREAK_TIMER) > 0;
    }

    private void updatePoise(){
        int break_time = getPoiseBreakTime();
        if (getEntity().isActiveItemStackBlocking()){
            if (entityData.getAbilityExecutor().isCasting()){
                entityData.getAbilityExecutor().interruptCast();
            }
            if (break_time > 0){
                getEntity().stopActiveHand();
            }
            return;
        }
        if (break_time > 0 || getPoiseRegenRate() <= 0f){
            return;
        }

        float max = getMaxPoise();
        if (getPoise() > max){
            setPoise(max);
        }

        if (getPoise() == max){
            return;
        }

        setPoise(Math.min(getPoise() + (getPoiseRegenRate() / 20.0f), max));
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

    public Tuple<Float, Boolean> handlePoiseDamage(float damageIn){
        float blockPortion = (float) (getEntity().getAttribute(MKAttributes.BLOCK_EFFICIENCY).getValue() * damageIn);
        float remainder = damageIn - blockPortion;
        float poise = getPoise();
        if (blockPortion >= poise){
            breakPoise();
            return new Tuple<>(remainder + blockPortion - poise, true);
        } else {
            if (getEntity().getItemInUseMaxCount() < 6){
                blockPortion *= 0.25f;
            }
            setPoise(poise - blockPortion);
            return new Tuple<>(remainder, false);
        }
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
