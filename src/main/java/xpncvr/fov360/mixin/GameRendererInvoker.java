package xpncvr.fov360.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {

	@Invoker("renderItemInHand")
	void panini$renderItemInHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix);

	@Invoker("extractCamera")
	void panini$extractCamera(DeltaTracker deltaTracker, float worldPartialTicks, float cameraEntityPartialTicks);

	@Accessor("hudProjection")
	Projection panini$hudProjection();

	@Accessor("hud3dProjectionMatrixBuffer")
	ProjectionMatrixBuffer panini$hud3dProjectionMatrixBuffer();

	@Accessor("screenEffectRenderer")
	ScreenEffectRenderer panini$screenEffectRenderer();

	@Accessor("handAndScreenSubmitNodeStorage")
	SubmitNodeStorage panini$handAndScreenSubmitNodeStorage();
}
