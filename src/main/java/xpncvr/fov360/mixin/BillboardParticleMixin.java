package xpncvr.fov360.mixin;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(BillboardParticle.class)
public abstract class BillboardParticleMixin {

	@Redirect(
		method = "render(Lnet/minecraft/client/particle/BillboardParticleSubmittable;Lnet/minecraft/client/render/Camera;F)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/particle/BillboardParticle$Rotator;setRotation(Lorg/joml/Quaternionf;Lnet/minecraft/client/render/Camera;F)V"))
	private void panini$billboardTowardEye(BillboardParticle.Rotator rotator, Quaternionf quaternion, Camera camera, float tickProgress) {
		boolean standard = Fov360Renderer.capturing
			&& (rotator == BillboardParticle.Rotator.ALL_AXIS || rotator == BillboardParticle.Rotator.Y_AND_W_ONLY);
		if (standard) {
			ParticleAccessor self = (ParticleAccessor) this;
			Vec3d eye = camera.getCameraPos();
			double px = MathHelper.lerp((double) tickProgress, self.panini$getLastX(), self.panini$getX());
			double py = MathHelper.lerp((double) tickProgress, self.panini$getLastY(), self.panini$getY());
			double pz = MathHelper.lerp((double) tickProgress, self.panini$getLastZ(), self.panini$getZ());
			Fov360Renderer.billboardRotation(
				quaternion,
				px - eye.getX(),
				py - eye.getY(),
				pz - eye.getZ(),
				rotator == BillboardParticle.Rotator.Y_AND_W_ONLY);
		} else {
			rotator.setRotation(quaternion, camera, tickProgress);
		}
	}
}
