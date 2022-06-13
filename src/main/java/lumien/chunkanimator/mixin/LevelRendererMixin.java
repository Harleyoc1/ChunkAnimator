package lumien.chunkanimator.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import lumien.chunkanimator.ChunkAnimator;
import lumien.chunkanimator.handler.PreRenderContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Harley O'Connor
 */
@Mixin(LevelRenderer.class)
public final class LevelRendererMixin {

    @Redirect(method = "renderChunkLayer", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FFF)V"
    ))
    private void preventDefaultOffset(Uniform chunkOffset, float x, float y, float z) {
        // Since this doesn't allow local capture and we need access to the renderChunk we simply do nothing here
        // and replace this with our own logic in #preRenderChunk.
    }

    @Inject(method = "renderChunkLayer", at = @At(
            value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lcom/mojang/blaze3d/shaders/Uniform;upload()V"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void preRenderChunk(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ,
                                Matrix4f projectionMatrix, CallbackInfo ci, boolean notTranslucent,
                                ObjectListIterator<LevelRenderer.RenderChunkInfo> renderChunkIterator,
                                VertexFormat typeFormat, ShaderInstance shaderInstance, Uniform chunkOffset,
                                boolean atLeastOneLayerDrawn, LevelRenderer.RenderChunkInfo renderChunkInfo,
                                ChunkRenderDispatcher.RenderChunk renderChunk, VertexBuffer chunkVertexBuffer,
                                BlockPos chunkOrigin) {
        ChunkAnimator.instance.animationHandler.preRender(
                new PreRenderContext(
                        renderChunk,
                        chunkOffset,
                        (float) (chunkOrigin.getX() - camX),
                        (float) (chunkOrigin.getY() - camY),
                        (float) (chunkOrigin.getZ() - camZ)
                )
        );
    }

}
