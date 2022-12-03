package lumien.chunkanimator.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import org.objectweb.asm.Opcodes;
import lumien.chunkanimator.ChunkAnimator;
import lumien.chunkanimator.handler.PreRenderContext;
import lumien.chunkanimator.util.OptiFineUniform3fWrapper;
import lumien.chunkanimator.util.VanillaUniformWrapper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

/**
 * {@link lumien.chunkanimator.mixin.LevelRendererMixin} equivalent for OptiFine.
 *
 * @author Harley O'Connor
 */
@Mixin(LevelRenderer.class)
public final class OptiFineLevelRendererMixin {

    private static final Field CHUNK_OFFSET_UNIFORM_FIELD;

    static {
        try {
            Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
            CHUNK_OFFSET_UNIFORM_FIELD = shadersClass.getField("uniform_chunkOffset");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private ChunkRenderDispatcher.RenderChunk currentRenderChunk;

    /**
     * Gets the current render chunk, saving it in {@link #currentRenderChunk} for use by the offset overrides.
     * This is done because {@link Redirect} cannot capture locals.
     */
    @Redirect(method = "renderChunkLayer", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/LevelRenderer$RenderChunkInfo;chunk:Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;",
            opcode = Opcodes.GETFIELD,
            ordinal = 3
    ))
    private ChunkRenderDispatcher.RenderChunk getRenderChunk(LevelRenderer.RenderChunkInfo owner) {
        currentRenderChunk = owner.chunk;
        return owner.chunk;
    }

    /**
     * Sets the chunk offset uniform with a modified x/y/z values, creating the animation.
     */
    @Redirect(method = "renderChunkLayer", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/shaders/Uniform;set(FFF)V"
    ))
    private void preRender(Uniform chunkOffset, float x, float y, float z) {
        ChunkAnimator.instance.animationHandler.preRender(
                new PreRenderContext(
                        currentRenderChunk,
                        new VanillaUniformWrapper(chunkOffset),
                        x,
                        y,
                        z
                )
        );
    }

    /**
     * Matches behaviour of {@link #preRender(Uniform, float, float, float)}, except sets OptiFine's shaders
     * chunk offset uniform instead. This makes the animation work when shaders are enabled.
     */
    @Redirect(method = "renderChunkLayer", at = @At(
            value = "INVOKE",
            target = "Lnet/optifine/shaders/uniform/ShaderUniform3f;setValue(FFF)V",
            ordinal = 0
    ))
    private void preRender(@Coerce Object chunkOffset, float x, float y, float z) {
        Object chunkOffsetUniform;
        try {
            chunkOffsetUniform = CHUNK_OFFSET_UNIFORM_FIELD.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ChunkAnimator.instance.animationHandler.preRender(
                new PreRenderContext(
                        currentRenderChunk,
                        new OptiFineUniform3fWrapper(chunkOffsetUniform),
                        x,
                        y,
                        z
                )
        );
    }

}
