package xpncvr.fov360.mixin;

import net.minecraft.client.render.entity.FireworkRocketEntityRenderer;
import net.minecraft.client.render.entity.state.FireworkRocketEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(FireworkRocketEntityRenderer.class)
public abstract class FireworkRocketEntityRendererMixin {

	@Redirect(
		method = "render(Lnet/minecraft/client/render/entity/state/FireworkRocketEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/render/state/CameraRenderState;orientation:Lorg/joml/Quaternionf;"))
	private Quaternionf panini$fireworkTowardEye(CameraRenderState cameraState, FireworkRocketEntityRenderState state, MatrixStack matrices) {
		return Fov360Renderer.billboardOrientation(matrices, cameraState.orientation, false);
	}
}
