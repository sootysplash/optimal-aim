package me.sootysplash.optaim.mixin;

import me.sootysplash.optaim.Client;
import me.sootysplash.optaim.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(GameConfig args, CallbackInfo ci) {
        Main.onInitialize();
    }

    // this makes it render on top of minecraft screens, workaround is to disable rendering when a minecraft screen is visible, side effect is minimal?
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateDisplay(Lcom/mojang/blaze3d/TracyFrameCapture;)V"))
    private void onRender2(CallbackInfo ci) {
        Client.renderOptimalAimBox();
    }

}
