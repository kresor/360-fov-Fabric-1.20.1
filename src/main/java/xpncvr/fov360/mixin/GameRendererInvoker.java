package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.ProjectionMatrix3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

	@Invoker("renderHand")
	void panini$renderHand(float tickProgress, boolean sleeping, Matrix4f positionMatrix);

	@Invoker("getFov")
	float panini$getFov(Camera camera, float tickProgress, boolean changingFov);

	@Accessor("hudProjectionMatrix")
	ProjectionMatrix3 panini$hudProjectionMatrix();
}
