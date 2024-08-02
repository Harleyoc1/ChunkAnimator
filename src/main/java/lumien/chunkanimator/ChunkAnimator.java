package lumien.chunkanimator;

import lumien.chunkanimator.config.ChunkAnimatorConfig;
import lumien.chunkanimator.handler.AnimationHandler;
import lumien.chunkanimator.handler.LevelEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * @author lumien231
 */
@Mod(ChunkAnimator.MOD_ID)
public final class ChunkAnimator {

	public static final String MOD_ID = "chunkanimator";

	public static ChunkAnimator instance;

	public AnimationHandler animationHandler;

	public ChunkAnimator(IEventBus modBus, ModContainer modContainer) {
		instance = this;

		modContainer.registerConfig(ModConfig.Type.CLIENT, ChunkAnimatorConfig.SPEC);

        modBus.addListener(this::setupClient);
	}

	/**
	 * Performs setup tasks that should only be run on the client. {@link net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection#setOrigin(int, int, int)}
	 *
	 * @param event The {@link FMLClientSetupEvent} instance.
	 */
	private void setupClient(final FMLClientSetupEvent event) {
		this.animationHandler = new AnimationHandler();

		NeoForge.EVENT_BUS.register(new LevelEventHandler());
	}

}
