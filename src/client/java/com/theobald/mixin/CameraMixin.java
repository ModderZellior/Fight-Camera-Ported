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
public abstract class CameraMixin {
    @Shadow
    private boolean detached;
    @Shadow
    private float xRot;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("HEAD"))
    private void disableDetachedCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        this.detached = false;
    }

    // has to run before the frustum + view matrix get built, otherwise we're a frame behind
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = At.Shift.AFTER))
    private void updateFightCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        FightCameraClient.onRender(deltaTracker.getGameTimeDeltaPartialTick(true));
        if (FightCameraClient.active) {
            setRotation(FightCameraClient.client.player.getYRot(), xRot);
        }
    }
}
