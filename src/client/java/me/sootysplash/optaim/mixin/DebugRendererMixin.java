package me.sootysplash.optaim.mixin;

import me.sootysplash.optaim.Client;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "emitGizmos", at = @At(value = "TAIL"))
    private void onRender2(CallbackInfo ci) {
        Client.renderOptimalAimBox();
    }

}
