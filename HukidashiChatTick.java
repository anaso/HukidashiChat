package anaso.HukidashiChat;

import java.util.HashMap;
import java.util.EnumSet;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL11.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.*;

public class HukidashiChatTick implements ITickHandler
{
	HashMap <String, Object> Options = new HashMap<String, Object>();

	private final EnumSet<TickType> tickSet = EnumSet.of(TickType.RENDER);

	static String[] listenerString = {"",""};
	
	int[] suspendTime = {0, 0, 0, 0};
	String[][] writingString = new String[4][2];
	
	ResourceLocation resourceLocation;
	
	public HukidashiChatTick(HashMap Options)
	{
		this.Options = Options;
		resourceLocation = new ResourceLocation("hukidashichat:textures/gui/hukidashi.png");
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public void renderHukidashiChat(String[] writeString, int suspendNumber)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		MC.func_110434_K().func_110577_a(resourceLocation);

		Gui gui = new Gui();
		gui.drawTexturedModalRect(45, 45, 0, 0, 120, 68);
		
		MC.fontRenderer.drawString(writeString[0], 54, 54, 65793);
		MC.fontRenderer.drawString(writeString[1], 54, 69, 65793);
		
		
		suspendTime[suspendNumber]--;
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		if(MC.theWorld != null)
		{
			if(MC.currentScreen == null)
			{
				if(!listenerString[0].equals(""))
				{
					//System.out.println(MC.ingameGUI.getChatGUI().getSentMessages());
					
					for(int i = 0; i < 4; i++)
					{
						if(suspendTime[i] <= 0)
						{
							writingString[i][0] = listenerString[0];
							writingString[i][1] = listenerString[1];
							listenerString[0] = "";
							listenerString[1] = "";
							suspendTime[i] = 200;
							
							break;
						}
					}
				}
				
				for(int i = 0; i < 4; i++)
				{
					if(suspendTime[i] > 0)
					{
						renderHukidashiChat(writingString[i], i);
					}
				}
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