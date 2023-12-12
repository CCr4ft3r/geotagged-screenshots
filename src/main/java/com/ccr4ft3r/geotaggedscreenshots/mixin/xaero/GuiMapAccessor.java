package com.ccr4ft3r.geotaggedscreenshots.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapMouseButtonPress;

@Mixin(GuiMap.class)
public interface GuiMapAccessor {

    @Accessor("leftMouseButton")
    MapMouseButtonPress leftMouseButton();

    @Accessor("mouseDownPosX")
    void setMouseDownPosX(int mouseDownPosX);

    @Accessor("mouseDownPosY")
    void setMouseDownPosY(int mouseDownPosY);
}