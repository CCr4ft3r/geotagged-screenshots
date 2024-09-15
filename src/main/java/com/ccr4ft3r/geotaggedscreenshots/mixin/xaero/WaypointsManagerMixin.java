package com.ccr4ft3r.geotaggedscreenshots.mixin.xaero;

import com.ccr4ft3r.geotaggedscreenshots.GeotaggedScreenshots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.waypoints.WaypointsManager;

@Mixin(WaypointsManager.class)
public abstract class WaypointsManagerMixin {

    @Inject(method = "updateWaypoints", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;isEmpty()Z", shift = At.Shift.AFTER), remap = false)
    private void onUpdatedWaypoints(CallbackInfo ci) {
        GeotaggedScreenshots.load();
    }
}