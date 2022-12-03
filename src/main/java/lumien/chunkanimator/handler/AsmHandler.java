package lumien.chunkanimator.handler;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import lumien.chunkanimator.ChunkAnimator;
import lumien.chunkanimator.util.VanillaUniformWrapper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class acts as a "middle man" between Minecraft's transformed classes and
 * the {@link AnimationHandler}.
 *
 * @author lumien231
 */
@OnlyIn(Dist.CLIENT)
public final class AsmHandler {

	private AsmHandler() {}

	private static final AnimationHandler HANDLER = ChunkAnimator.instance.animationHandler;

	/**
	 * Calls {@link AnimationHandler#setOrigin(ChunkRenderDispatcher.RenderChunk, BlockPos)} with
	 * the given parameters.
	 *
	 * <p>The {@link ChunkRenderDispatcher.RenderChunk} transformer invokes this method, inserting
	 * the call in {@link ChunkRenderDispatcher.RenderChunk#setOrigin(int, int, int)}.</p>
	 *
	 * @param renderChunk The {@link ChunkRenderDispatcher.RenderChunk} instance.
	 * @param x The {@code x-coordinate} for the origin.
	 * @param y The {@code y-coordinate} for the origin.
	 * @param z The {@code z-coordinate} for the origin.
	 */
	public static void setOrigin(ChunkRenderDispatcher.RenderChunk renderChunk, int x, int y, int z) {
		HANDLER.setOrigin(renderChunk, new BlockPos(x, y, z));
	}

	/**
	 * Calls {@link AnimationHandler#preRender(PreRenderContext)}
	 * with the given parameters.
	 *
	 * <p>The {@link LevelRenderer} transformer invokes this method, replacing the default
	 * {@link Uniform#set(float, float, float)} call in
	 * {@link LevelRenderer#renderChunkLayer(RenderType, PoseStack, double, double, double, Matrix4f)}.
	 *
	 * @param uniform The chunk offset {@link Uniform} object.
	 * @param x The final x-coordinate for the chunk (where it should end up).
	 * @param y The final y-coordinate for the chunk (where it should end up).
	 * @param z The final z-coordinate for the chunk (where it should end up).
	 * @param renderChunk The {@link ChunkRenderDispatcher.RenderChunk} instance.
	 */
	public static void preRenderChunk(final Uniform uniform, final float x, final float y, final float z,
									  final ChunkRenderDispatcher.RenderChunk renderChunk) {
		HANDLER.preRender(new PreRenderContext(renderChunk, new VanillaUniformWrapper(uniform), x, y, z));
	}

}
