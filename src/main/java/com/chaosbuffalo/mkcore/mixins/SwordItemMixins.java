package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemMixins extends TieredItem {

    public SwordItemMixins(IItemTier tierIn, Properties builder) {
        super(tierIn, builder);
    }

    /**
     * @author kovak
     * @reason change sword to block when used
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    /**
     * @author kovak
     * @reason give use duration same as shield
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }


    /**
     * @author kovak
     * @reason make sword block only when we are not poise broke and shield is not in offhand
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        ItemStack offhand = playerIn.getHeldItemOffhand();
        if (offhand.getItem() instanceof ShieldItem){
            return ActionResult.resultPass(itemstack);
        }
        if (MKCore.getPlayer(playerIn).map(x -> x.getStats().isPoiseBroke()).orElse(false)){
            return ActionResult.resultPass(itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return ActionResult.resultConsume(itemstack);
        }
    }
}
