package xpncvr.fov360.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin {

	@Redirect(
		method = "extract(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState;Lnet/minecraft/client/Camera;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/particle/SingleQuadParticle$FacingCameraMode;setRotation(Lorg/joml/Quaternionf;Lnet/minecraft/client/Camera;F)V"))
	private void panini$billboardTowardEye(SingleQuadParticle.FacingCameraMode mode, Quaternionf quaternion, Camera camera, float partialTickTime) {
		boolean standard = Fov360Renderer.capturing
			&& (mode == SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ || mode == SingleQuadParticle.FacingCameraMode.LOOKAT_Y);
		if (standard) {
			ParticleAccessor self = (ParticleAccessor) this;
			Vec3 eye = camera.position();
			double px = Mth.lerp((double) partialTickTime, self.panini$getXOld(), self.panini$getX());
			double py = Mth.lerp((double) partialTickTime, self.panini$getYOld(), self.panini$getY());
			double pz = Mth.lerp((double) partialTickTime, self.panini$getZOld(), self.panini$getZ());
			Fov360Renderer.billboardRotation(
				quaternion,
				px - eye.x(),
				py - eye.y(),
				pz - eye.z(),
				mode == SingleQuadParticle.FacingCameraMode.LOOKAT_Y);
		} else {
			mode.setRotation(quaternion, camera, partialTickTime);
		}
	}
}
