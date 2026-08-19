package xpncvr.fov360.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {

	@Redirect(
		method = "getBaseColor",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;forwardVector()Lorg/joml/Vector3fc;"))
	private Vector3fc panini$sunsetTintFromTrueView(Camera camera) {
		if (Fov360Renderer.capturing) {
			return Fov360Renderer.captureViewForward;
		}
		return camera.forwardVector();
	}
}
