package xpncvr.fov360.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

	@Accessor("xo")
	double panini$getXOld();

	@Accessor("yo")
	double panini$getYOld();

	@Accessor("zo")
	double panini$getZOld();

	@Accessor("x")
	double panini$getX();

	@Accessor("y")
	double panini$getY();

	@Accessor("z")
	double panini$getZ();
}
