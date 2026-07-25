package xpncvr.fov360.mixin;

import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

	@ModifyArg(
		method = "<init>",
		slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.fov")),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/SimpleOption$ValidatingIntSliderCallbacks;<init>(II)V",
			ordinal = 0
		),
		index = 1
	)
	private int panini$raiseFovMax(int maxInclusive) {
		return 400;
	}
}
