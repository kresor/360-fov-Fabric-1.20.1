package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 1.20.1-only accessors used by the panoramic renderer for the final hand pass. */
@Mixin(GameRenderer.class)
public interface HandRendererInvoker {
    @Invoker("renderHand")
    void fov360$renderHand(MatrixStack matrices, Camera camera, float tickDelta);

    @Invoker("getFov")
    double fov360$getFov(Camera camera, float tickDelta, boolean changingFov);
}
