package com.theobald.mixin;

import com.theobald.FightCameraClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Unique
    LocalPlayer LocalPlayer = Minecraft.getInstance().player;

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void forceRenderNametag(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LocalPlayer && FightCameraClient.active) {
            cir.setReturnValue(true); // Always show the name tag for the local player
        }
    }
}