package lumien.chunkanimator.handler;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;

/**
 * Holds context parameters required for animating chunks.
 *
 * @author Harley O'Connor
 */
public record AnimationContext(
        SectionRenderDispatcher.RenderSection renderSection,
        Uniform uniform,
        float x,
        float y,
        float z,
        AnimationHandler.AnimationData animationData,
        BlockPos origin,
        float timeDif,
        LevelContext levelContext
) {

    public record LevelContext(
            double horizonHeight,
            int minY,
            int maxY
    ) {

        public static LevelContext from(ClientLevel level) {
            return new LevelContext(level.getLevelData().getHorizonHeight(level), level.dimensionType().minY(),
                    level.dimensionType().minY() + level.dimensionType().height());
        }

    }

}
