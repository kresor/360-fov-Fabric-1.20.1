package xpncvr.fov360.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "renderWorld", at = @At("HEAD"), cancellable = true)
	private void panini$driveWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
		if (Fov360Renderer.capturing) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (!Fov360Renderer.INSTANCE.shouldRun(client)) {
			return;
		}
		if (Fov360Renderer.INSTANCE.runFrame((GameRenderer) (Object) this, tickCounter)) {
			ci.cancel();
		}
	}

	@Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
	private void panini$projectionFov(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
		GameRenderer self = (GameRenderer) (Object) this;
		if (Fov360Renderer.capturing) {
			cir.setReturnValue(new Matrix4f().perspective(
				Math.min(fovDegrees, 90.0F) * (float) (Math.PI / 180.0),
				1.0F,
				0.05F,
				self.getFarPlaneDistance()));
		} else if (fovDegrees > 150.0F) {
			MinecraftClient client = MinecraftClient.getInstance();
			float aspect = (float) client.getWindow().getFramebufferWidth()
				/ (float) client.getWindow().getFramebufferHeight();
			cir.setReturnValue(new Matrix4f().perspective(
				150.0F * (float) (Math.PI / 180.0),
				aspect,
				0.05F,
				self.getFarPlaneDistance()));
		}
	}

	@Inject(method = "getProjectionMatrix", at = @At("HEAD"), cancellable = true)
	private void panini$captureCullFov(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
		if (Fov360Renderer.capturing) {
			cir.setReturnValue(new Matrix4f().perspective(
				110.0F * (float) (Math.PI / 180.0),
				1.0F,
				0.05F,
				((GameRenderer) (Object) this).getFarPlaneDistance()));
		}
	}

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void panini$clampVanillaFov(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		if (cir.getReturnValueF() > 150.0F) {
			cir.setReturnValue(150.0F);
		}
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void panini$cancelBob(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
		if (Fov360Renderer.capturing) {
			ci.cancel();
		}
	}

	@Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
	private void panini$cancelTilt(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
		if (Fov360Renderer.capturing) {
			ci.cancel();
		}
	}
}
