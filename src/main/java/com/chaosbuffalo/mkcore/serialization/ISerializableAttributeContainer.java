package com.chaosbuffalo.mkcore.serialization;

import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;

import java.util.List;

public interface ISerializableAttributeContainer {

    List<ISerializableAttribute<?>> getAttributes();

    void addAttribute(ISerializableAttribute<?> attribute);

    void addAttributes(ISerializableAttribute<?>... attributes);
}
