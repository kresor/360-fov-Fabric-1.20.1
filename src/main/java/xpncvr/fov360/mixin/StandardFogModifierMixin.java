package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.StandardFogModifier;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(StandardFogModifier.class)
public abstract class StandardFogModifierMixin {

	@Redirect(
		method = "getFogColor",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/Camera;getHorizontalPlane()Lorg/joml/Vector3f;"))
	private Vector3f panini$sunsetTintFromTrueView(Camera camera) {
		if (Fov360Renderer.capturing) {
			return Fov360Renderer.captureViewForward;
		}
		return camera.getHorizontalPlane();
	}
}
