package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {

	@Invoker("setRotation")
	void panini$setRotation(float yaw, float pitch);
}
