package com.ccr4ft3r.geotaggedscreenshots.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.map.mods.SupportXaeroMinimap;

@Mixin(SupportXaeroMinimap.class)
public interface SupportXaeroMinimapAccessor {

    @Accessor("waypointSet")
    WaypointSet waypointSet();

    @Accessor("modMain")
    IXaeroMinimap modMain();

    @Accessor("waypointWorld")
    WaypointWorld waypointWorld();
}