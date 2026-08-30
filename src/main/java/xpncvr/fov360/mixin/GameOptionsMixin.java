package xpncvr.fov360.mixin;

import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Extends Minecraft 1.20.1's normal FOV option from 30..110 to 30..400.
 *
 * This deliberately modifies the existing vanilla option instead of adding a
 * second config screen. The value therefore lives in options.txt and the
 * ordinary Options -> FOV slider becomes the 360-FOV control.
 */
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
    private int fov360$raiseFovMaximum(int vanillaMaximum) {
        return 400;
    }
}
