package xpncvr.fov360.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xpncvr.fov360.Fov360Renderer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	private static final double CAPTURE_CULL_MARGIN = 16.0;

	@Redirect(
		method = "shouldRender",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/Frustum;isVisible(Lnet/minecraft/util/math/Box;)Z"))
	private boolean panini$captureCullMargin(Frustum frustum, Box box) {
		if (Fov360Renderer.capturing) {
			return frustum.isVisible(box.expand(CAPTURE_CULL_MARGIN));
		}
		return frustum.isVisible(box);
	}
}
