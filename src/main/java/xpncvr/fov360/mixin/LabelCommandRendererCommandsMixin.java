package xpncvr.fov360.mixin;

import net.minecraft.client.render.command.LabelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(LabelCommandRenderer.Commands.class)
public abstract class LabelCommandRendererCommandsMixin {

	@Unique
	private static final Quaternionf panini$look = new Quaternionf();

	@Redirect(
		method = "add",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V"))
	private void panini$labelTowardEye(MatrixStack instance, Quaternionfc orientation, MatrixStack matrices, Vec3d pos) {
		if (Fov360Renderer.capturing && pos != null) {
			Matrix4f m = instance.peek().getPositionMatrix();
			Fov360Renderer.billboardRotation(panini$look, m.m30(), m.m31(), m.m32(), false);
			instance.multiply(panini$look);
		} else {
			instance.multiply(orientation);
		}
	}
}
