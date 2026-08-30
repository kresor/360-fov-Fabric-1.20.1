package xpncvr.fov360.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
    private void fov360$overrideFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        Framebuffer target = Fov360Renderer.getCaptureTargetOverride();
        if (target != null) {
            cir.setReturnValue(target);
        }
    }
}
