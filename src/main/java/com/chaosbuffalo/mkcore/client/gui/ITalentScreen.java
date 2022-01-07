package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;

public interface ITalentScreen {
    TalentTreeRecord getCurrentTree();

    void setCurrentTree(TalentTreeRecord currentTree);
}
