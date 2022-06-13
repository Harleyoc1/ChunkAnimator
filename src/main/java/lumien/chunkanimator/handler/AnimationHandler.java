package lumien.chunkanimator.handler;

import lumien.chunkanimator.config.AnimationMode;
import lumien.chunkanimator.config.ChunkAnimatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.WeakHashMap;

/**
 * This class handles setting up and rendering the animations.
 *
 * @author lumien231
 */
@OnlyIn(Dist.CLIENT)
public final class AnimationHandler {

	private final Minecraft mc = Minecraft.getInstance();
	private final WeakHashMap<ChunkRenderDispatcher.RenderChunk, AnimationData> timeStamps = new WeakHashMap<>();

	public void preRender(PreRenderContext context) {
		final var animationData = timeStamps.get(context.renderChunk());

		if (animationData == null) {
			context.uniform().set(context.x(), context.y(), context.z());
			return;
		}

		final var mode = ChunkAnimatorConfig.MODE.get();
		final int animationDuration = ChunkAnimatorConfig.ANIMATION_DURATION.get();

		long time = animationData.timeStamp;

		// If preRender hasn't been called on this chunk yet, prepare to start the animation.
		if (time == -1L) {
			time = System.currentTimeMillis();
			animationData.timeStamp = time;
			mode.prepareConsumer().accept(context, animationData);
		}

		final long timeDif = System.currentTimeMillis() - time;

		if (timeDif < animationDuration) {
			ChunkAnimatorConfig.MODE.get().contextConsumer().accept(new AnimationContext(
					context.renderChunk(),
					context.uniform(),
					context.x(),
					context.y(),
					context.z(),
					animationData,
					context.renderChunk().getOrigin(),
					timeDif,
					AnimationContext.LevelContext.from(Objects.requireNonNull(this.mc.level))
			));
		} else {
			context.uniform().set(context.x(), context.y(), context.z());
			this.timeStamps.remove(context.renderChunk());
		}
	}

	public void setOrigin(final ChunkRenderDispatcher.RenderChunk renderChunk, final BlockPos pos) {
		if (this.mc.player == null)
			return;

		final BlockPos zeroedPlayerPos = getZeroedPlayerPos(this.mc.player);
		final BlockPos zeroedCenteredChunkPos = getZeroedCenteredChunkPos(pos);

		if (!ChunkAnimatorConfig.DISABLE_AROUND_PLAYER.get() || zeroedPlayerPos.distSqr(zeroedCenteredChunkPos) > (64 * 64)) {
			timeStamps.put(renderChunk, new AnimationData(-1L, ChunkAnimatorConfig.MODE.get() == AnimationMode.HORIZONTAL_SLIDE ?
					getChunkFacing(zeroedPlayerPos.subtract(zeroedCenteredChunkPos)) : null));
		} else {
			timeStamps.remove(renderChunk);
		}
	}

	/**
	 * Gets the given player's position, setting their {@code y-coordinate} to {@code 0}.
	 *
	 * @param player The {@link LocalPlayer} instance.
	 * @return The zeroed {@link BlockPos}.
	 */
	public static BlockPos getZeroedPlayerPos (final LocalPlayer player) {
		final BlockPos playerPos = player.blockPosition();
		return playerPos.offset(0, -playerPos.getY(), 0);
	}

	/**
	 * Gets the given {@link BlockPos} for the chunk, setting its {@code y-coordinate} to
	 * {@code 0} and offsetting its {@code x} and {@code y-coordinate} to by {@code 8}.
	 *
	 * @param position The {@link BlockPos} of the chunk.
	 * @return The zeroed, centered {@link BlockPos}.
	 */
	public static BlockPos getZeroedCenteredChunkPos(final BlockPos position) {
		return position.offset(8, -position.getY(), 8);
	}

	/**
	 * Gets the direction the chunk is facing based on the given {@link Vec3i}
	 * from the relevant position to the chunk.
	 *
	 * @param dif The {@link Vec3i} distance from the relevant position to the chunk.
	 * @return The {@link Direction} of the chunk relative to the {@code dif}.
	 */
	public static Direction getChunkFacing(final Vec3i dif) {
		final int difX = Math.abs(dif.getX());
		final int difZ = Math.abs(dif.getZ());

		return difX > difZ ? dif.getX() > 0 ? Direction.EAST : Direction.WEST : dif.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
	}

	public void clear () {
		// These should be cleared by GC, but just in case.
		this.timeStamps.clear();
	}

	public static class AnimationData {
		public long timeStamp;
		public Direction chunkFacing;

		public AnimationData(final long timeStamp, final Direction chunkFacing) {
			this.timeStamp = timeStamp;
			this.chunkFacing = chunkFacing;
		}
	}

}
