package xpncvr.fov360.mixin;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Options.class)
public abstract class OptionsMixin {

	@ModifyArg(
		method = "<init>",
		slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.fov")),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/OptionInstance$IntRange;<init>(II)V",
			ordinal = 0
		),
		index = 1
	)
	private int panini$raiseFovMax(int maxInclusive) {
		return 400;
	}
}
