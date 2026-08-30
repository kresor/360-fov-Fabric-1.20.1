package xpncvr.fov360.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Inject(method = "getFramebufferWidth", at = @At("HEAD"), cancellable = true)
    private void fov360$captureWidth(CallbackInfoReturnable<Integer> cir) {
        int size = Fov360Renderer.getActiveCaptureSize();
        if (size > 0) {
            cir.setReturnValue(size);
        }
    }

    @Inject(method = "getFramebufferHeight", at = @At("HEAD"), cancellable = true)
    private void fov360$captureHeight(CallbackInfoReturnable<Integer> cir) {
        int size = Fov360Renderer.getActiveCaptureSize();
        if (size > 0) {
            cir.setReturnValue(size);
        }
    }
}
