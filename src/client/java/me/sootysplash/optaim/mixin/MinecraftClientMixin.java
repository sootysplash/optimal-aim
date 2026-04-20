package me.sootysplash.optaim.mixin;

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

}
