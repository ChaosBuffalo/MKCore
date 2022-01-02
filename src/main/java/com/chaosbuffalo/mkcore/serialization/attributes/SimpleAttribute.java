package com.chaosbuffalo.mkcore.serialization.attributes;


import java.util.function.Consumer;

public abstract class SimpleAttribute<T> implements ISerializableAttribute<T> {
    private T defaultValue;
    private T currentValue;
    private final String name;
    private Consumer<ISerializableAttribute<T>> onSetCallback;

    public SimpleAttribute(String name, T defaultValue) {
        this.defaultValue = defaultValue;
        currentValue = defaultValue;
        this.name = name;
        onSetCallback = null;
    }

    @Override
    public void setDefaultValue(T newValue) {
        this.defaultValue = newValue;
        currentValue = defaultValue;
    }

    public void setOnSetCallback(Consumer<ISerializableAttribute<T>> onSetCallback) {
        this.onSetCallback = onSetCallback;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T getValue() {
        return currentValue;
    }

    @Override
    public void setValue(T newValue) {
        this.currentValue = newValue;
        if (onSetCallback != null) {
            onSetCallback.accept(this);
        }
    }
}
