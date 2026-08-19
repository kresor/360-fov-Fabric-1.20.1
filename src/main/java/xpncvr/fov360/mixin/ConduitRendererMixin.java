package xpncvr.fov360.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.state.ConduitRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(ConduitRenderer.class)
public abstract class ConduitRendererMixin {

	@Redirect(
		method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ConduitRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;orientation:Lorg/joml/Quaternionf;"))
	private Quaternionf panini$conduitWindTowardEye(CameraRenderState cameraState, ConduitRenderState state, PoseStack poseStack) {
		return Fov360Renderer.billboardOrientation(poseStack, cameraState.orientation, false);
	}
}
