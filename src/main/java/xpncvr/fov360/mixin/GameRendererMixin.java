package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xpncvr.fov360.Fov360Renderer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    private boolean renderHand;

    /**
     * Replace the one world-render invocation inside GameRenderer.render with our
     * cube capture + reprojection. GUI rendering continues normally afterwards.
     */
    @Redirect(
        method = "render(FJZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"
        )
    )
    private void fov360$redirectWorldRender(GameRenderer gameRenderer, float tickDelta, long limitTime, MatrixStack matrices) {
        boolean oldRenderHand = this.renderHand;
        try {
            Fov360Renderer.INSTANCE.renderFrame(gameRenderer, tickDelta, limitTime, matrices, oldRenderHand);
        } finally {
            gameRenderer.setRenderHand(oldRenderHand);
        }
    }

    /** Force each cube face to use a true 90 degree capture projection. */
    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void fov360$captureFov(Camera camera, float tickDelta, boolean changingFov,
                                   CallbackInfoReturnable<Double> cir) {
        if (Fov360Renderer.CAPTURING && changingFov) {
            cir.setReturnValue(90.0D);
        }
    }
}
