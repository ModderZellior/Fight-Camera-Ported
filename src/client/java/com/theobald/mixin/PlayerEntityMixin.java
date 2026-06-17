package com.theobald.mixin;

import com.theobald.FightCameraClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LocalPlayer.class, priority = 1101)
public abstract class PlayerEntityMixin {
    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void disableMovement(CallbackInfo ci) {
        if (FightCameraClient.active) {
            ci.cancel();
        }
    }
}