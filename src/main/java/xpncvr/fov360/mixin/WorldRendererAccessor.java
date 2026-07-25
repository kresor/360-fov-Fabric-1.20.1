package xpncvr.fov360.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

	@Accessor("entityOutlineFramebuffer")
	Framebuffer panini$getEntityOutlineFramebuffer();

	@Accessor("entityOutlineFramebuffer")
	void panini$setEntityOutlineFramebuffer(Framebuffer framebuffer);
}
