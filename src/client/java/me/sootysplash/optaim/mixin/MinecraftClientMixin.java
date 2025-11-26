package me.sootysplash.optaim.mixin;

import me.sootysplash.optaim.Client;
import me.sootysplash.optaim.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RunArgs args, CallbackInfo ci) {
        Main.onInitialize();
    }

    // this makes it render on top of minecraft screens, workaround is to disable rendering when a minecraft screen is visible, side effect is minimal?
    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V"))
    private void onRender2(CallbackInfo ci) {
        Client.renderOptimalAimBox();
    }

}
