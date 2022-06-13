package lumien.chunkanimator.config;

import lumien.chunkanimator.handler.AnimationContext;
import lumien.chunkanimator.handler.AnimationHandler;
import lumien.chunkanimator.handler.PreRenderContext;
import net.minecraft.client.Minecraft;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static lumien.chunkanimator.handler.AnimationHandler.*;

/**
 * @author Harley O'Connor
 */
public enum AnimationMode {
    BELOW(context -> context.uniform().set(
            context.x(),
            context.y() - Math.abs(context.origin().getY()) + getFunctionValue(
                    context.timeDif(),
                    0,
                    Math.abs(context.origin().getY()),
                    ChunkAnimatorConfig.ANIMATION_DURATION.get()
            ),
            context.z()
    )),
    ABOVE(context -> context.uniform().set(
            context.x(),
            context.y() + context.levelContext().maxY() - Math.abs(context.origin().getY()) - getFunctionValue(
                    context.timeDif(),
                    0,
                    context.levelContext().maxY() - Math.abs(context.origin().getY()),
                    ChunkAnimatorConfig.ANIMATION_DURATION.get()
            ),
            context.z()
    )),
    HYBRID(context -> {
        if (context.origin().getY() < context.levelContext().horizonHeight()) {
            BELOW.contextConsumer.accept(context);
        } else {
            ABOVE.contextConsumer.accept(context);
        }
    }),
    HORIZONTAL_SLIDE(context -> {
        final var chunkFacing = context.animationData().chunkFacing;
        if (chunkFacing != null) {
            final var vec = chunkFacing.getNormal();
            final var mod = -(200F - getFunctionValue(context.timeDif(), 0, 200, ChunkAnimatorConfig.ANIMATION_DURATION.get()));

            context.uniform().set(context.x() + vec.getX() * mod, context.y(), context.z() +  vec.getZ() * mod);
        }
    }),
    HORIZONTAL_SLIDE_ALTERNATE(
            (context, data) ->
                    data.chunkFacing = getChunkFacing(getZeroedPlayerPos(Objects.requireNonNull(Minecraft.getInstance().player))
                            .subtract(getZeroedCenteredChunkPos(context.renderChunk().getOrigin()))
                    ),
            HORIZONTAL_SLIDE.contextConsumer
    );

    private final BiConsumer<PreRenderContext, AnimationHandler.AnimationData> prepareConsumer;
    private final Consumer<AnimationContext> contextConsumer;

    AnimationMode(Consumer<AnimationContext> contextConsumer) {
        this((context, data) -> {}, contextConsumer);
    }

    AnimationMode(BiConsumer<PreRenderContext, AnimationHandler.AnimationData> prepareConsumer, Consumer<AnimationContext> contextConsumer) {
        this.prepareConsumer = prepareConsumer;
        this.contextConsumer = contextConsumer;
    }

    public BiConsumer<PreRenderContext, AnimationHandler.AnimationData> prepareConsumer() {
        return prepareConsumer;
    }

    public Consumer<AnimationContext> contextConsumer() {
        return contextConsumer;
    }

    private static float getFunctionValue(final float t, @SuppressWarnings("SameParameterValue") final float b, final float c, final float d) {
        return ChunkAnimatorConfig.EASING_FUNCTION.get().easeOutFunc().apply(t, b, c, d);
    }
}
