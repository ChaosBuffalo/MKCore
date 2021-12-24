package com.chaosbuffalo.mkcore.test.v2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NewHealEffect extends MKEffect {
    public static final NewHealEffect INSTANCE = new NewHealEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKEffect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private NewHealEffect() {
        super(EffectType.BENEFICIAL);
        setRegistryName("effect.new_heal");
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        if (targetData.getEffects().isEffectActive(SkinLikeWoodEffectV2.INSTANCE)) {
            MKCore.LOGGER.info("NewHealEffect.onInstanceAdded found SkinLikeWoodEffectV2 so adding an extra stack");
            newInstance.modifyDuration(300);
            newInstance.modifyStackCount(1);
        }
    }

    @Override
    public Instance createInstance(UUID sourceId) {
        return new Instance(this, sourceId);
    }

    public Instance createInstance(MKAbility ability, Entity source) {
        return new Instance(this, source).forAbility(ability);
    }

    public static class Instance extends MKEffectInstance {

        private float base = 0.0f;
        private float scale = 1.0f;
        private ResourceLocation abilityId = MKCoreRegistry.INVALID_ABILITY;
        private Entity source;

        public Instance(MKEffect effect, UUID caster) {
            super(effect, caster);
            source = null;
        }

        public Instance(MKEffect effect, Entity source) {
            super(effect, source.getUniqueID());
            this.source = source;
        }

        public Instance forAbility(MKAbility ability) {
            abilityId = ability.getAbilityId();
            return this;
        }

        public Instance forAbility(ResourceLocation abilityId) {
            this.abilityId = abilityId;
            return this;
        }

        public Instance configure(float base, float scale) {
            this.base = base;
            this.scale = scale;
            return this;
        }

        @Override
        protected CompoundNBT serializeState() {
            CompoundNBT tag = super.serializeState();
            tag.putFloat("base", base);
            tag.putFloat("scale", scale);
            tag.putString("abilityId", abilityId.toString());
            return tag;
        }

        @Override
        public void deserializeState(CompoundNBT stateTag) {
            super.deserializeState(stateTag);
            base = stateTag.getFloat("base");
            scale = stateTag.getFloat("scale");
            abilityId = ResourceLocation.tryCreate(stateTag.getString("abilityId"));
            if (abilityId == null)
                abilityId = MKCoreRegistry.INVALID_ABILITY;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            source = findEntity(source, sourceId, targetData);
//            MKCore.LOGGER.info("NewHealEffect.performEffect trying to recover source {} = {}", sourceId, source);

            LivingEntity target = targetData.getEntity();
            MKCore.LOGGER.info("NewHealEffect.performEffect on {} from {} {}", target, source, instance);
            float value = base + (scale * instance.getStackCount());
            MKHealSource heal = MKHealSource.getHolyHeal(abilityId, source, 1.0f);
            MKHealing.healEntityFrom(target, value, heal);
            return true;
        }
    }
}
