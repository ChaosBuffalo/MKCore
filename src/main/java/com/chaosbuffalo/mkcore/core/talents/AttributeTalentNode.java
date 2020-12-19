package com.chaosbuffalo.mkcore.core.talents;


import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class AttributeTalentNode extends TalentNode {

    private final double perRank;

    public AttributeTalentNode(AttributeTalent talent, Dynamic<?> dynamic) {
        super(talent, dynamic);
        this.perRank = dynamic.get("value").asDouble(talent.getDefaultPerRank());
    }

    public double getValue(int rank) {
        return perRank * rank;
    }

    public double getPerRank() {
        return perRank;
    }

    @Override
    public AttributeTalent getTalent() {
        return (AttributeTalent) super.getTalent();
    }

    public <T> T serialize(DynamicOps<T> ops) {
        T value = super.serialize(ops);
        value = ops.mergeToMap(value, ops.createString("value"), ops.createDouble(perRank)).resultOrPartial(MKCore.LOGGER::error).get();
        return value;
    }
}
