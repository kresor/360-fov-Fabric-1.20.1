package xpncvr.fov360.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 1.20.1-only invoker with a unique filename to avoid colliding with the upstream 26.x invoker. */
@Mixin(GameRenderer.class)
public interface HandRendererInvoker {
    @Invoker("renderHand")
    void fov360$renderHand(MatrixStack matrices, Camera camera, float tickDelta);
}
