package lumien.chunkanimator.mixin;

import lumien.chunkanimator.ChunkAnimator;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Harley O'Connor
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public final class RenderSectionMixin {

    @Inject(method = "setOrigin", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;reset()V"
    ))
    public void setOrigin(int x, int y, int z, CallbackInfo ci) {
        ChunkAnimator.instance.animationHandler.setOrigin(
                (SectionRenderDispatcher.RenderSection) (Object) this,
                new BlockPos(x, y, z)
        );
    }

}
