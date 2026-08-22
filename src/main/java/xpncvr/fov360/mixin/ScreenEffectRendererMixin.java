package xpncvr.fov360.mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xpncvr.fov360.Fov360Renderer;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void panini$cancelScreenEffectsDuringCapture(boolean isFirstPerson, boolean isSleeping,
			float partialTicks, SubmitNodeCollector submitNodeCollector, boolean hideGui, CallbackInfo ci) {
		if (Fov360Renderer.capturing) {
			ci.cancel();
		}
	}
}
