package anaso.HukidashiChat;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.EnumSet;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION;
import org.bouncycastle.util.Arrays;
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
	String[][] writingString;
	
	int[][] guiPosition = {{30,30},{230,30},{30,130},{230,130}};
	// X, Y
	
	int alpha = 255;
	int displayTime = 200;
	int[][] guiRawPosition = new int[4][2];
	int[] textureSize;
	int[] textPosition;
	
	double[][] playerPos = {{0,0},{0,0},{0,0},{0,0}};
	
	int nameColor = 0;
	int textColor = 0;
	
	boolean nameShadow = false;
	boolean textShadow = false;
	
	int stringColumn = 5;
	int stringWidth = 83;
	
	ResourceLocation resourceLocation;
	
	public HukidashiChatTick(HashMap Options)
	{
		this.Options = Options;
		
		guiPosition = (int[][])Options.get("GuiPosition");
		
		for (int i = 0; i < guiPosition.length; i++)
		{
			System.arraycopy(guiPosition[i], 0, guiRawPosition[i], 0, guiPosition[i].length);
		}
		
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
		
		nameColor = ((tempNameColor[0] & 255) << 16) + ((tempNameColor[1] & 255) << 8) + (tempNameColor[2] & 255) + ((alpha & 255) << 24);
		textColor = ((tempTextColor[0] & 255) << 16) + ((tempTextColor[1] & 255) << 8) + (tempTextColor[2] & 255) + ((alpha & 255) << 24);
		
		System.out.println(nameColor + " : " + textColor + " : " + tempNameColor[3] + " : " + tempTextColor[3]);
		
		resourceLocation = new ResourceLocation("hukidashichat:textures/gui/hukidashi.png");
		
		writingString = new String[4][stringColumn];
		for(int i = 0; i < 4; i ++)
		{
			for(int j = 0; j < stringColumn; j ++)
			{
				writingString[i][j] = "";
			}
		}
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
		for(int i = 1; i < stringColumn; i++)
		{
			if(!writeString[i].equals(""))
			{
				MC.fontRenderer.drawString(writeString[i], guiPosition[suspendNumber][0] + textPosition[2], guiPosition[suspendNumber][1] + textPosition[3] + ((i-1) * MC.fontRenderer.FONT_HEIGHT), textColor, textShadow);
			}
		}
		
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
							for(int j = 0; j < stringColumn; j ++)
							{
								writingString[i][j] = "";  //初期化
							}
							
							writingString[i][0] = listenerString[0];
							writingString[i][1] = listenerString[1];
							listenerString[0] = "";
							listenerString[1] = "";
							suspendTime[i] = displayTime;
							ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
							// GUIスケールに合わせて調整
							
							//System.out.println(MC.fontRenderer.trimStringToWidth(writingString[i][1], 80));
							
							for(int j = 1; j < stringColumn; j++)
							{
								// 複数行の調整
								if(MC.fontRenderer.getStringWidth(writingString[i][j]) > stringWidth)
								{
									String trimString = MC.fontRenderer.trimStringToWidth(writingString[i][j], stringWidth);
									if(j < stringColumn - 1)
									{
										writingString[i][j+1] = writingString[i][j].substring(trimString.length());
									}
									writingString[i][j] = trimString;
								}
							}
							
							// 位置の調整
							if(guiRawPosition[i][0] < 0)
							{
								guiPosition[i][0] = SR.getScaledWidth() + guiRawPosition[i][0] - textureSize[0];
							}
							if(guiRawPosition[i][1] < 0)
							{
								guiPosition[i][1] = SR.getScaledHeight() + guiRawPosition[i][1] - textureSize[1];
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