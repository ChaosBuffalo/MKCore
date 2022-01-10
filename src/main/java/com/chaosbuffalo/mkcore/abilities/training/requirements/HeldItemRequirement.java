package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class HeldItemRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.held_item");
    private Item item;
    private Hand hand;

    public HeldItemRequirement(Item item, Hand hand) {
        super(TYPE_NAME);
        this.item = item;
        this.hand = hand;
    }

    public <D> HeldItemRequirement(Dynamic<D> dynamic){
        super(TYPE_NAME, dynamic);
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        ItemStack stack = playerData.getEntity().getHeldItem(hand);
        if (stack.isEmpty())
            return false;

        return stack.getItem() == item;
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {

    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("item"), ops.createString(item.getRegistryName().toString()));
        builder.put(ops.createString("hand"), ops.createInt(hand.ordinal()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        dynamic.get("item").asString().result().ifPresent(x -> {
            this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(x));
        });
        this.hand = Hand.values()[dynamic.get("hand").asInt(0)];

    }

    @Override
    public IFormattableTextComponent describe(MKPlayerData playerData) {
        String handName = hand == Hand.MAIN_HAND ? "Main" : "Off";
        return new StringTextComponent("You must be holding a ")
                .appendSibling(new TranslationTextComponent(item.getTranslationKey())) // Item.getName is client-only
                .appendSibling(new StringTextComponent(String.format(" in your %s hand", handName)));
    }
}
