package com.theobald.mixin;

import com.theobald.FightCameraClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private boolean detached;

    @Inject(method = "update", at = @At("HEAD"))
    private void disableDetachedCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        this.detached = false;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void updateFightCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        FightCameraClient.onRender(deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}