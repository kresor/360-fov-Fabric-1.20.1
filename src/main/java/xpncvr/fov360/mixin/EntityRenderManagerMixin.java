package xpncvr.fov360.mixin;

import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {

	@Redirect(
		method = "render",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/render/state/CameraRenderState;orientation:Lorg/joml/Quaternionf;"))
	private Quaternionf panini$fireTowardEye(
		CameraRenderState owner,
		EntityRenderState renderState,
		CameraRenderState cameraRenderState,
		double d,
		double e,
		double f,
		MatrixStack matrixStack) {
		return Fov360Renderer.billboardOrientation(matrixStack, owner.orientation, true);
	}
}
