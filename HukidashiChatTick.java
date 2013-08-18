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
	
	int[][] guiPosition = {{30,30},{230,30},{30,130},{230,130}};
	// X, Y
	
	int alpha = 255;
	int displayTime = 200;
	int[][] guiOptionPosition;
	int[] textureSize;
	int[] textPosition;
	
	int nameColor = 0;
	int textColor = 0;
	
	boolean nameShadow = false;
	boolean textShadow = false;
	
	ResourceLocation resourceLocation;
	
	public HukidashiChatTick(HashMap Options)
	{
		this.Options = Options;
		
		guiOptionPosition = (int[][])Options.get("GuiPosition");
		guiPosition = (int[][])Options.get("GuiPosition");
		alpha = (Integer)(Options.get("Alpha"));
		displayTime = (Integer)(Options.get("DisplayTime"));
		textureSize = (int[])Options.get("TextureSize");
		textPosition = (int[])Options.get("TextPosition");
		int[] tempNameColor = (int[])Options.get("NameColor");
		int[] tempTextColor = (int[])Options.get("TextColor");
		
		if(tempNameColor[3] != 0)
		{
			nameShadow = true;
		}
		if(tempTextColor[3] != 0)
		{
			textShadow = true;
		}
		
		/*
		if(alpha < 0 || alpha > 255)
		{
			alpha = 255;
		}
		
		for(int i = 0; i < 3; i++)
		{
			if(tempNameColor[i] < 0 || tempNameColor[i] > 255)
			{
				tempNameColor[i] = 255;
			}
			if(tempTextColor[i] < 0 || tempTextColor[i] > 255)
			{
				tempTextColor[i] = 0;
			}
		}
		*/
		
		nameColor = ((tempNameColor[0] & 255) << 16) + ((tempNameColor[1] & 255) << 8) + (tempNameColor[2] & 255) + ((alpha & 255) << 24);
		textColor = ((tempTextColor[0] & 255) << 16) + ((tempTextColor[1] & 255) << 8) + (tempTextColor[2] & 255) + ((alpha & 255) << 24);
		
		System.out.println(nameColor + " : " + textColor + " : " + tempNameColor[3] + " : " + tempTextColor[3]);
		
		resourceLocation = new ResourceLocation("hukidashichat:textures/gui/hukidashi.png");
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	public void renderHukidashiChat(String[] writeString, int suspendNumber)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha/255);
		MC.func_110434_K().func_110577_a(resourceLocation);

		Gui gui = new Gui();
		gui.drawTexturedModalRect(guiPosition[suspendNumber][0], guiPosition[suspendNumber][1], 0, 0, textureSize[0], textureSize[1]);
		
		MC.fontRenderer.drawString(writeString[0], guiPosition[suspendNumber][0] + textPosition[0], guiPosition[suspendNumber][1] + textPosition[1], nameColor, nameShadow);
		MC.fontRenderer.drawString(writeString[1], guiPosition[suspendNumber][0] + textPosition[2], guiPosition[suspendNumber][1] + textPosition[3], textColor, textShadow);
		
		// System.out.println(MC.theWorld.playerEntities);
		
		// 16777215 white
		// 65793 black
		
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
							suspendTime[i] = displayTime;
							ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
							
							if(guiOptionPosition[i][0] < 0)
							{
								guiPosition[i][0] = SR.getScaledWidth() + guiOptionPosition[i][0] - textureSize[0];
								System.out.println(guiPosition[i][0]);
							}
							if(guiOptionPosition[i][1] < 0)
							{
								guiPosition[i][1] = SR.getScaledHeight() + guiOptionPosition[i][1] - textureSize[1];
								System.out.println(guiPosition[i][1]);
							}
							
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
	public String getLabel()
	{
		return null;
	}
}