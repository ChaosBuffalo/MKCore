package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;

public interface IAbilityScreen extends IMKScreen {
    boolean shouldAbilityDrag();
    void setDragging(MKAbility dragging);
    MKAbility getAbility();
    void setAbility(MKAbility ability);
    void clearDragging();
    boolean isDraggingAbility();
    MKAbility getDragging();
}
