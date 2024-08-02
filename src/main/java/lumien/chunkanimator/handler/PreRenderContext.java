package lumien.chunkanimator.handler;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

/**
 * Holds context gathered from the {@link LevelRenderer} required for pre-rendering
 * the chunk.
 *
 * @author Harley O'Connor
 */
public record PreRenderContext(
        SectionRenderDispatcher.RenderSection renderSection,
        Uniform uniform,
        float x,
        float y,
        float z
) { }
