package xpncvr.fov360.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(Mouse.class)
public abstract class MouseMixin {

	@Shadow private double x;

	@Inject(method = "scaleX", at = @At("HEAD"), cancellable = true)
	private static void panini$scaleXHalf(Window window, double x, CallbackInfoReturnable<Double> cir) {
		if (Fov360Renderer.splitGuiActive()) {
			double originX = Fov360Renderer.splitGuiOnRight() ? window.getWidth() / 2.0 : 0.0;
			cir.setReturnValue((x - originX) * window.getScaledWidth() / (window.getWidth() / 2.0));
		}
	}

	@Redirect(
		method = "unlockCursor",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(Lnet/minecraft/client/util/Window;IDD)V"))
	private void panini$centerCursorOnForwardHalf(Window window, int mode, double x, double y) {
		if (Fov360Renderer.splitGuiActive()) {
			double forwardCenterX = window.getWidth() * (Fov360Renderer.splitGuiOnRight() ? 0.75 : 0.25);
			this.x = forwardCenterX;
			InputUtil.setCursorParameters(window, mode, forwardCenterX, y);
		} else {
			InputUtil.setCursorParameters(window, mode, x, y);
		}
	}
}
