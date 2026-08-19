package xpncvr.fov360.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.phys.AABB;
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
			target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z"))
	private boolean panini$captureCullMargin(Frustum frustum, AABB box) {
		if (Fov360Renderer.capturing) {
			return frustum.isVisible(box.inflate(CAPTURE_CULL_MARGIN));
		}
		return frustum.isVisible(box);
	}
}
