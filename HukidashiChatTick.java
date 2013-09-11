package anaso.HukidashiChat;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.EnumSet;

import javax.annotation.processing.RoundEnvironment;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.*;

public class HukidashiChatTick implements ITickHandler
{
	HashMap <String, Object> Options = new HashMap<String, Object>();

	private final EnumSet<TickType> tickSet = EnumSet.of(TickType.RENDER);

	static String[] listenerString = {"",""};
	static boolean listenerViewMessage = false;
	
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
	
	int stringColumn = 4;
	int stringWidth = 83;
	
	int[] hukidashiScale = {10,30};
	
	ResourceLocation guiHukidashiMain;
	ResourceLocation guiHukidashi;
	
	boolean enableAllMessage = false;
	double playerSpaceOption;
	
	int gameSettingFov;
	float fovPerWidth;
	
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
		
		stringColumn = (Integer)Options.get("StringColumn");
		stringWidth = (Integer)Options.get("StringWidth");
		
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
		
		//System.out.println(nameColor + " : " + textColor + " : " + tempNameColor[3] + " : " + tempTextColor[3]);
		
		enableAllMessage = Boolean.parseBoolean((String)Options.get("ViewAllMessage"));
		
		playerSpaceOption = (double)(Integer)Options.get("PlayerSpace");
		
		guiHukidashiMain = new ResourceLocation("hukidashichat:textures/gui/hukidashi_gui.png");
		guiHukidashi = new ResourceLocation("hukidashichat:textures/gui/hukidashi.png");
		
		stringColumn++; // 配列の0が名前のため、1つ多くしておく
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
		MC.func_110434_K().func_110577_a(guiHukidashiMain);

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
		
		if(!enableAllMessage && !writeString[0].equals(""))
		{
			try
			{
				EntityPlayer player = MC.theWorld.getPlayerEntityByName(writeString[0]);
				
				if(player.worldObj.getWorldInfo().getWorldName().equals(MC.thePlayer.worldObj.getWorldInfo().getWorldName()) && player.dimension == MC.thePlayer.dimension)
				{
					double playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - player.posX, 2) + Math.pow(MC.thePlayer.posY - player.posY, 2) + Math.pow(MC.thePlayer.posZ - player.posZ, 2));
					
					if(playerSpaceOption > playerSpace)
					{
						//System.out.println("in hukidashi");
						
						GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha/255);
						MC.func_110434_K().func_110577_a(guiHukidashi);
						gui.drawTexturedModalRect(guiPosition[suspendNumber][0] + textureSize[0] - 50, guiPosition[suspendNumber][1] + textureSize[1] - 50, 0, 0, 80, 80);
						
						//        N x:0 z:-1
						// W x:-1 z:0      E x:1 z:0
						//        S x:0 z:1
						//      Up y:1  Down y:-1
						
						if(checkHukidashiView(MC))
						{
							
						}
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e);
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
				
				gameSettingFov = 70 + (int)(MC.gameSettings.fovSetting * 40);
				fovPerWidth = (gameSettingFov * 2) / MC.displayWidth;
				// 横1ピクセルあたりの角度
				
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

	private boolean checkHukidashiView(Minecraft MC)
	{
		// 画面内に相手プレイヤーがいるかの確認
		
		float viewYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);
		
		boolean returnBoolean = false;
		
		if(viewYaw < 0)
		{
			viewYaw += 180;
		}
		else if(viewYaw > 0)
		{
			viewYaw -= 180;
		}
		
		float playerSpaceYaw = (float) (-Math.atan2(MC.thePlayer.posX, MC.thePlayer.posZ) / Math.PI * 180);
		System.out.println(viewYaw + " : " + MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationPitch));
		System.out.println(playerSpaceYaw + " : " + Math.atan2(-MC.thePlayer.posY, -MC.thePlayer.posX) / Math.PI * 180);
		
		if (viewYaw < playerSpaceYaw + gameSettingFov && viewYaw > playerSpaceYaw - gameSettingFov)
		{
			returnBoolean = true;
		}
		else if(180 < playerSpaceYaw + gameSettingFov && (playerSpaceYaw + gameSettingFov) > viewYaw + 360)
		{
			returnBoolean = true;
		}
		else if(-180 > playerSpaceYaw - gameSettingFov && (playerSpaceYaw - gameSettingFov) < viewYaw - 360)
		{
			returnBoolean = true;
		}
		
		if(returnBoolean)
		{
			System.out.println("in");
		}
		
		return returnBoolean;
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