package lumien.chunkanimator.handler;

import java.util.WeakHashMap;

import org.lwjgl.util.vector.Vector3f;

import lumien.chunkanimator.ChunkAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import penner.easing.Back;
import penner.easing.Bounce;
import penner.easing.Circ;
import penner.easing.Cubic;
import penner.easing.Elastic;
import penner.easing.Expo;
import penner.easing.Linear;
import penner.easing.Quad;
import penner.easing.Quart;
import penner.easing.Quint;
import penner.easing.Sine;

public class AnimationHandler
{
	WeakHashMap<Object, AnimationData> timeStamps;

	public AnimationHandler()
	{
		timeStamps = new WeakHashMap<Object, AnimationData>();
	}

	public void preRender(Object renderChunk, BlockPos pos)
	{
		Vector3f offset = getOffset(renderChunk, pos);
		if (offset != null)
		{
			GlStateManager.translate(offset.x, offset.y, offset.z);
		}
	}

	public Vector3f getOffset(Object renderChunk, BlockPos position)
	{
		if (timeStamps.containsKey(renderChunk))
		{
			AnimationData animationData = timeStamps.get(renderChunk);
			long time = animationData.timeStamp;
			int mode = ChunkAnimator.INSTANCE.config.getMode();

			if (time == -1L)
			{
				time = System.currentTimeMillis();

				animationData.timeStamp = time;

				// Mode 4 Set Chunk Facing
				if (mode == 4)
				{
					BlockPos zeroedPlayerPosition = Minecraft.getMinecraft().player.getPosition();
					zeroedPlayerPosition = zeroedPlayerPosition.add(0, -zeroedPlayerPosition.getY(), 0);

					BlockPos zeroedCenteredChunkPos = position.add(8, -position.getY(), 8);

					Vec3i dif = zeroedPlayerPosition.subtract(zeroedCenteredChunkPos);

					int difX = Math.abs(dif.getX());
					int difZ = Math.abs(dif.getZ());

					EnumFacing chunkFacing;

					if (difX > difZ)
					{
						if (dif.getX() > 0)
						{
							chunkFacing = EnumFacing.EAST;
						}
						else
						{
							chunkFacing = EnumFacing.WEST;
						}
					}
					else
					{
						if (dif.getZ() > 0)
						{
							chunkFacing = EnumFacing.SOUTH;
						}
						else
						{
							chunkFacing = EnumFacing.NORTH;
						}
					}

					animationData.chunkFacing = chunkFacing;
				}
			}

			long timeDif = System.currentTimeMillis() - time;

			int animationDuration = ChunkAnimator.INSTANCE.config.getAnimationDuration();

			if (timeDif < animationDuration)
			{
				int chunkY = position.getY();
				double modY;

				if (mode == 2)
				{
					if (chunkY < Minecraft.getMinecraft().world.provider.getHorizon())
					{
						mode = 0;
					}
					else
					{
						mode = 1;
					}
				}

				if (mode == 4)
				{
					mode = 3;
				}

				switch (mode)
				{
					case 0:
						return new Vector3f(0, -chunkY + getFunctionValue(timeDif, 0, chunkY, animationDuration), 0);
					case 1:
						return new Vector3f(0, 256 - chunkY - getFunctionValue(timeDif, 0, 256 - chunkY, animationDuration), 0);
					case 3:
						EnumFacing chunkFacing = animationData.chunkFacing;

						if (chunkFacing != null)
						{
							Vec3i vec = chunkFacing.getDirectionVec();
							float mod = -(200F - (200F / animationDuration * timeDif));

							mod = -(200 - getFunctionValue(timeDif, 0, 200, animationDuration));

							return new Vector3f(vec.getX() * mod, 0, vec.getZ() * mod);
						}
						break;
				}
			}
			else
			{
				timeStamps.remove(renderChunk);
			}
		}
		return null;
	}

	private float getFunctionValue(float t, float b, float c, float d)
	{
		switch (ChunkAnimator.INSTANCE.config.getEasingFunction())
		{
			case 0: // Linear
				return Linear.easeOut(t, b, c, d);
			case 1: // Quadratic Out
				return Quad.easeOut(t, b, c, d);
			case 2: // Cubic Out
				return Cubic.easeOut(t, b, c, d);
			case 3: // Quartic Out
				return Quart.easeOut(t, b, c, d);
			case 4: // Quintic Out
				return Quint.easeOut(t, b, c, d);
			case 5: // Expo Out
				return Expo.easeOut(t, b, c, d);
			case 6: // Sin Out
				return Sine.easeOut(t, b, c, d);
			case 7: // Circle Out
				return Circ.easeOut(t, b, c, d);
			case 8: // Back
				return Back.easeOut(t, b, c, d);
			case 9: // Bounce
				return Bounce.easeOut(t, b, c, d);
			case 10: // Elastic
				return Elastic.easeOut(t, b, c, d);
		}

		return Sine.easeOut(t, b, c, d);
	}

	public void setOrigin(Object renderChunk, BlockPos position)
	{
		if (Minecraft.getMinecraft().player != null)
		{
			boolean flag = true;
			BlockPos zeroedPlayerPosition = Minecraft.getMinecraft().player.getPosition();
			zeroedPlayerPosition = zeroedPlayerPosition.add(0, -zeroedPlayerPosition.getY(), 0);
			BlockPos zeroedCenteredChunkPos = position.add(8, -position.getY(), 8);

			if (ChunkAnimator.INSTANCE.config.disableAroundPlayer())
			{
				flag = zeroedPlayerPosition.distanceSq(zeroedCenteredChunkPos) > (64 * 64);
			}

			if (flag)
			{
				EnumFacing chunkFacing = null;

				if (ChunkAnimator.INSTANCE.config.getMode() == 3)
				{
					Vec3i dif = zeroedPlayerPosition.subtract(zeroedCenteredChunkPos);

					int difX = Math.abs(dif.getX());
					int difZ = Math.abs(dif.getZ());

					if (difX > difZ)
					{
						if (dif.getX() > 0)
						{
							chunkFacing = EnumFacing.EAST;
						}
						else
						{
							chunkFacing = EnumFacing.WEST;
						}
					}
					else
					{
						if (dif.getZ() > 0)
						{
							chunkFacing = EnumFacing.SOUTH;
						}
						else
						{
							chunkFacing = EnumFacing.NORTH;
						}
					}
				}

				AnimationData animationData = new AnimationData(-1L, chunkFacing);
				timeStamps.put(renderChunk, animationData);
			}
			else
			{
				if (timeStamps.containsKey(renderChunk))
				{
					timeStamps.remove(renderChunk);
				}
			}
		}

	}

	private class AnimationData
	{
		public long timeStamp;

		public EnumFacing chunkFacing;

		public AnimationData(long timeStamp, EnumFacing chunkFacing)
		{
			this.timeStamp = timeStamp;
			this.chunkFacing = chunkFacing;
		}
	}
}
