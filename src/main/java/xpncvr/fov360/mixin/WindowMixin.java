package xpncvr.fov360.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(Window.class)
public abstract class WindowMixin {

	@Inject(method = "getScaledWidth", at = @At("HEAD"), cancellable = true)
	private void panini$halfScaledWidth(CallbackInfoReturnable<Integer> cir) {
		if (Fov360Renderer.splitGuiActive()) {
			Window self = (Window) (Object) this;
			double d = self.getScaleFactor();
			double halfWidth = self.getFramebufferWidth() / 2.0;
			int i = (int) (halfWidth / d);
			cir.setReturnValue(halfWidth / d > i ? i + 1 : i);
		}
	}
}
