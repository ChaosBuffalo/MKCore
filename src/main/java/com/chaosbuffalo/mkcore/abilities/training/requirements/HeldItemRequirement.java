package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HeldItemRequirement implements IAbilityTrainingRequirement {
    private final Item item;
    private final Hand hand;

    public HeldItemRequirement(Item item, Hand hand) {
        this.item = item;
        this.hand = hand;
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
    public ITextComponent describe(MKPlayerData playerData) {
        String handName = hand == Hand.MAIN_HAND ? "Main" : "Off";
        return new StringTextComponent("You must be holding a ")
                .appendSibling(new TranslationTextComponent(item.getTranslationKey())) // Item.getName is client-only
                .appendSibling(new StringTextComponent(String.format(" in your %s hand", handName)));
    }
}
