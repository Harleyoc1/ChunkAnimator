package lumien.chunkanimator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * @author lumien231
 */
public final class ChunkAnimatorConfig {

	/** The animation mode - controls how the chunks should be animated. */
	public static final ModConfigSpec.EnumValue<AnimationMode> MODE;

	/** The easing function - controls which easing function should be used. */
	public static final ModConfigSpec.EnumValue<EasingFunction> EASING_FUNCTION;

	/** The animation duration - controls how long the animation should last (in milliseconds). */
	public static final ModConfigSpec.IntValue ANIMATION_DURATION;
	
	/** Disable around player - disables animation of chunks near the player. */
	public static final ModConfigSpec.BooleanValue DISABLE_AROUND_PLAYER;

	public static final ModConfigSpec SPEC;

	static {
		final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

		MODE = builder.comment("""
				Defines how the chunks should be animated.
				 BELOW: Chunks always appear from below.
				 ABOVE: Chunks always appear from above.
				 HYBRID: Chunks appear from below if they are lower than the horizon and from above if they are higher than the horizon.
				 HORIZONTAL_SLIDE: Chunks "slide in" from their respective cardinal direction (relative to the player).
				 HORIZONTAL_SLIDE_ALTERNATE: Same as HORIZONTAL_SLIDE, but the cardinal direction of a chunk is determined slightly different (just try both :D).
				 """).defineEnum("mode", AnimationMode.BELOW);

		EASING_FUNCTION = builder.comment("""
				Defines the function that should be used to control the movement of chunks.
				If you want a visual comparison there is a link on the CurseForge page.
				""").defineEnum("easingFunction", EasingFunction.SINE);

		ANIMATION_DURATION = builder.comment("Defines how long the animation should last (in milliseconds).")
				.defineInRange("animationDuration", 1000, 0, Integer.MAX_VALUE);

		DISABLE_AROUND_PLAYER = builder.comment("If enabled, chunks that are next to the player will not animate.")
				.define("disableAroundPlayer", false);

		SPEC = builder.build();
	}

	private ChunkAnimatorConfig() {}

}
