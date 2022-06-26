package lumien.chunkanimator.handler;

import lumien.chunkanimator.ChunkAnimator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;

public class AsmHandler
{
	public static void preRenderChunk(RenderChunk renderChunk)
	{
		ChunkAnimator.INSTANCE.animationHandler.preRender(renderChunk, renderChunk.getPosition());
	}

	public static void setOrigin(RenderChunk renderChunk, int oX, int oY, int oZ)
	{
		ChunkAnimator.INSTANCE.animationHandler.setOrigin(renderChunk, new BlockPos(oX, oY, oZ));
	}
}
