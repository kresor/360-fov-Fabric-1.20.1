package xpncvr.fov360.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

	@Accessor("lastX")
	double panini$getLastX();

	@Accessor("lastY")
	double panini$getLastY();

	@Accessor("lastZ")
	double panini$getLastZ();

	@Accessor("x")
	double panini$getX();

	@Accessor("y")
	double panini$getY();

	@Accessor("z")
	double panini$getZ();
}
