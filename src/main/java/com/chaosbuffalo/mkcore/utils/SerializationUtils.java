package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;

public class SerializationUtils {

    public static ITextComponent fromCompoundNbt(CompoundNBT nbt){
        return nbt.toFormattedComponent();
    }

    public static CompoundNBT fromJsonString(String nbtString) throws CommandSyntaxException {
        return JsonToNBT.getTagFromJson(nbtString);
    }

    public static <D> D serializeItemStack(DynamicOps<D> ops, ItemStack stack) {
        CompoundNBT nbt = new CompoundNBT();
        stack.write(nbt);
        return ops.createString(fromCompoundNbt(nbt).getString());
    }

    public static <D> ItemStack deserializeItemStack(Dynamic<D> dynamic){
        Optional<String> nbtString = dynamic.asString().result();
        if (nbtString.isPresent()){
            try {
                CompoundNBT nbt = fromJsonString(nbtString.get());
                return ItemStack.read(nbt);
            } catch (CommandSyntaxException e) {
                MKCore.LOGGER.error("Failed to deserialize nbt string {}",
                        e.getMessage());
            }
        }
        return ItemStack.EMPTY;
    }
}
