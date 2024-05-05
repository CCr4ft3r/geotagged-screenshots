package com.ccr4ft3r.geotaggedscreenshots.mixin;

import com.ccr4ft3r.geotaggedscreenshots.config.ClientConfig;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.stb.STBImageWrite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NativeImage.class)
public class NativeImageMixin {

    @Redirect(method = "writeToChannel", at = @At(value = "INVOKE", target = "Lorg/lwjgl/stb/STBImageWrite;nstbi_write_png_to_func(JJIIIJI)I"))
    private int writeTo(long var0, long var2, int var4, int var5, int var6, long var7, int var9) {
        if (ClientConfig.CONFIG_DATA.useJpgForScreenshots.get())
            return STBImageWrite.nstbi_write_jpg_to_func(var0, var2, var4, var5, var6, var7, var9);
        return STBImageWrite.nstbi_write_png_to_func(var0, var2, var4, var5, var6, var7, var9);
    }
}