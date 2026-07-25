package xpncvr.fov360.mixin;

import net.minecraft.client.render.entity.DragonFireballEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(DragonFireballEntityRenderer.class)
public abstract class DragonFireballEntityRendererMixin {

	@Redirect(
		method = "render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/render/state/CameraRenderState;orientation:Lorg/joml/Quaternionf;"))
	private Quaternionf panini$dragonFireballTowardEye(CameraRenderState cameraState, EntityRenderState state, MatrixStack matrices) {
		return Fov360Renderer.billboardOrientation(matrices, cameraState.orientation, false);
	}
}
