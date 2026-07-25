package xpncvr.fov360.mixin;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Inject(method = "update", at = @At("TAIL"))
	private void panini$overrideCaptureRotation(CallbackInfo ci) {
		if (Fov360Renderer.capturing) {
			this.setRotation(Fov360Renderer.captureFaceYaw, Fov360Renderer.captureFacePitch);
		}
	}

	@Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
	private void panini$stableSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
		if (Fov360Renderer.capturing) {
			cir.setReturnValue(Fov360Renderer.capturedSubmersionType);
		}
	}
}
