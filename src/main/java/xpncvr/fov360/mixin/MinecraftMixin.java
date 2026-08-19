package xpncvr.fov360.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Inject(method = "getMainRenderTarget", at = @At("HEAD"), cancellable = true)
	private void panini$redirectRenderTarget(CallbackInfoReturnable<RenderTarget> cir) {
		RenderTarget target = Fov360Renderer.currentTarget;
		if (target != null) {
			cir.setReturnValue(target);
		}
	}
}
