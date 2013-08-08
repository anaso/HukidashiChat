package anaso.HukidashiChat;

import java.util.HashMap;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.EnumMovingObjectType;
import cpw.mods.fml.common.*;

public class HukidashiChatTick implements ITickHandler
{
	HashMap <String, int[]> Options = new HashMap<String, int[]>();

	private final EnumSet<TickType> tickSet = EnumSet.of(TickType.RENDER);


	public HukidashiChatTick(HashMap Options)
	{
		this.Options = Options;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
	}

	public void renderHukidashiChat()
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		MC.fontRenderer.drawStringWithShadow("Test", 100, 100, 16777215);

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		if(MC.theWorld != null)
		{
			if(MC.currentScreen == null)
			{
				//System.out.println(MC.ingameGUI.getChatGUI().getSentMessages());

				renderHukidashiChat();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return tickSet;
	}

	@Override
	public String getLabel() { return null; }
}