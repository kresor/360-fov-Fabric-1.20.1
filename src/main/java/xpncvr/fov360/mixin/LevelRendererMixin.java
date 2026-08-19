package xpncvr.fov360.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(method = "shouldShowEntityOutlines", at = @At("HEAD"), cancellable = true)
	private void panini$enableOutlinesDuringCapture(CallbackInfoReturnable<Boolean> cir) {
		if (Fov360Renderer.capturing && Fov360Renderer.captureOutlines) {
			cir.setReturnValue(true);
		}
	}
}
