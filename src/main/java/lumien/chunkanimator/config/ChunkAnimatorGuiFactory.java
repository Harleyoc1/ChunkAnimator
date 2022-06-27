package lumien.chunkanimator.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ChunkAnimatorGuiFactory implements IModGuiFactory
{

	public ChunkAnimatorGuiFactory()
	{
	}

	@Override
	public void initialize(Minecraft minecraftInstance)
	{
	}

	@Override
	public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories()
	{
		return null;
	}

	@Override
	public boolean hasConfigGui()
	{
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen)
	{
		return new ChunkAnimatorConfigGui(parentScreen);
	}

}
