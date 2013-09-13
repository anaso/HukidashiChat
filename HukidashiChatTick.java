package anaso.HukidashiChat;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.EnumSet;

import javax.annotation.processing.RoundEnvironment;

import org.apache.commons.lang3.BooleanUtils;
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

	static String[] listenerString = {"",""};  // リスナーから受け取る文字列
	//static boolean listenerViewMessage = false;  // 
	
	int[] suspendTime = {0, 0, 0, 0};  // 表示時間の保持
	String[][] writingString;  // 表示するメッセージの保持  [メッセージの番号][0名前 1テキスト]
	
	int[][] guiPosition = {{30,30},{230,30},{30,130},{230,130}};  // GUIの初期位置
	// X, Y
	
	int alpha = 255;  // 透過率 Max255
	int[] displayTime = {0, 200, 20};  // 初期表示時間
	int[][] guiRawPosition = new int[4][2];  // GUIの位置(コンフィグの値)
	int[] textureSize;  // メインのテクスチャサイズ
	int[] textPosition;  // GUI内のテキストの位置
	int[] hukidashiSize;  // 吹き出しのテクスチャサイズ
	int[] hukidashiRotation;  // 吹き出しの回転軸の位置
	
	//double[][] playerPos = {{0,0},{0,0},{0,0},{0,0}};
	
	int nameColor = 0;  // 名前の文字色
	int textColor = 0;  // テキストの文字色
	
	int[] nameColorSplit, textColorSplit;  // テキストの色 Max255
	
	boolean nameShadow = false;  // 名前に影をつけるか
	boolean textShadow = false;  // テキストに影をつけるか
	
	int stringColumn = 4;  // テキストの列
	int stringWidth = 83;  // 横の長さ
	
	//int[] hukidashiScale = {10,30};  // 
	
	ResourceLocation guiHukidashiMain;
	ResourceLocation guiHukidashi;
	
	boolean enableAllMessage = false;  //全てのメッセージを表示するか
	boolean viewHukidashi = true;  // 吹き出しを表示するか
	double playerSpaceOption;  // プレイヤー間の距離、コンフィグの値
	
	int gameSettingFov;
	float widthPerFov;  // 画面1ピクセルあたりの視野角
	
	public HukidashiChatTick(HashMap Options)
	{
		this.Options = Options;
		
		guiPosition = (int[][])Options.get("GuiPosition");
		
		for (int i = 0; i < guiPosition.length; i++)
		{
			System.arraycopy(guiPosition[i], 0, guiRawPosition[i], 0, guiPosition[i].length);
		}
		
		alpha = (Integer)(Options.get("Alpha"));
		displayTime = (int[])(Options.get("DisplayTime"));
		textureSize = (int[])Options.get("TextureSize");
		hukidashiSize = (int[])Options.get("HukidashiSize");
		hukidashiRotation = (int[])Options.get("HukidashiRotation");
		
		textPosition = (int[])Options.get("TextPosition");
		nameColorSplit = (int[])Options.get("NameColor");
		textColorSplit = (int[])Options.get("TextColor");
		
		stringColumn = (Integer)Options.get("StringColumn");
		stringWidth = (Integer)Options.get("StringWidth");
		
		
		
		if(nameColorSplit[3] != 0)
		{
			nameShadow = true;
		}
		if(textColorSplit[3] != 0)
		{
			textShadow = true;
		}
		
		nameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((alpha & 255) << 24);
		textColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((alpha & 255) << 24);
		
		//System.out.println(nameColor + " : " + textColor + " : " + nameColorSplit[3] + " : " + textColorSplit[3]);
		
		enableAllMessage = Boolean.parseBoolean((String)Options.get("ViewAllMessage"));
		viewHukidashi = Boolean.parseBoolean((String)Options.get("ViewHukidashi"));
		
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

	public void renderHukidashiChat(String[] writeString, int suspendNumber, float alphaFloat)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha * alphaFloat /255);
		MC.func_110434_K().func_110577_a(guiHukidashiMain);

		Gui gui = new Gui();
		gui.drawTexturedModalRect(guiPosition[suspendNumber][0], guiPosition[suspendNumber][1], 0, 0, textureSize[0], textureSize[1]);
		
		int fadeAlpha = (int)(alpha * alphaFloat);
		
		int tempNameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);
		int tempTextColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);
		// フェードイン、フェードアウト用
		
		MC.fontRenderer.drawString(writeString[0], guiPosition[suspendNumber][0] + textPosition[0], guiPosition[suspendNumber][1] + textPosition[1], tempNameColor, nameShadow);
		for(int i = 1; i < stringColumn; i++)
		{
			if(!writeString[i].equals(""))
			{
				MC.fontRenderer.drawString(writeString[i], guiPosition[suspendNumber][0] + textPosition[2], guiPosition[suspendNumber][1] + textPosition[3] + ((i-1) * MC.fontRenderer.FONT_HEIGHT), tempTextColor, textShadow);
			}
		}
		
		if(!enableAllMessage && !writeString[0].equals("") && viewHukidashi)
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
						int[] hukidashiPixels = checkHukidashiView(MC, player, playerSpace);
						
						if(hukidashiPixels[0] == 1)
						{
							ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
							float SRMagnification = MC.displayHeight / SR.getScaledHeight();
							MC.fontRenderer.drawString("o", ((int)((SR.getScaledWidth() / 2) + (hukidashiPixels[1] / SRMagnification))), ((int)((SR.getScaledHeight() / 2) + (hukidashiPixels[2] / SRMagnification))), 16777215);
							System.out.println(SR.getScaledWidth() + " : " + SR.getScaledHeight() + " : " + SRMagnification);
							
							GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha * alphaFloat /255);
							MC.func_110434_K().func_110577_a(guiHukidashi);
							
							GL11.glTranslatef(guiPosition[suspendNumber][0] + textureSize[0] - 3, guiPosition[suspendNumber][1] + textureSize[1] - 3, 0);
							GL11.glRotatef(suspendTime[suspendNumber] * 3, 0, 0, 1.0F);
							GL11.glTranslatef(-4, -4, 0);
							
							//gui.drawTexturedModalRect(guiPosition[suspendNumber][0] + textureSize[0] - 8, guiPosition[suspendNumber][1] + textureSize[1] - 8, 0, 0, hukidashiSize[0], hukidashiSize[1]);
							gui.drawTexturedModalRect(0, 0, 0, 0, hukidashiSize[0], hukidashiSize[1]);
							
							//        N x:0 z:-1
							// W x:-1 z:0      E x:1 z:0
							//        S x:0 z:1
							//      Up y:1  Down y:-1
							
							GL11.glTranslatef(4, 4, 0);
							GL11.glRotatef(-suspendTime[suspendNumber] * 3, 0, 0, 1.0F);
							GL11.glTranslatef(-(guiPosition[suspendNumber][0] + textureSize[0] - 3), -(guiPosition[suspendNumber][1] + textureSize[1] - 3), 0);
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
							suspendTime[i] = displayTime[0] + displayTime[1] + displayTime[2];
							ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
							// GUIスケールに合わせて調整
							
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
				//widthPerFov = ((float)MC.displayWidth / gameSettingFov);
				widthPerFov = ((float)new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight).getScaledWidth() / (float)gameSettingFov);
				// 横1ピクセルあたりの角度
				
				for(int i = 0; i < 4; i++)
				{
					if(suspendTime[i] > 0 && suspendTime[i] > displayTime[1] + displayTime[2])
					{
						renderHukidashiChat(writingString[i], i, 1.0F - (float)(suspendTime[i] - displayTime[1] - displayTime[2]) / (float)displayTime[0]);
					}
					else if(suspendTime[i] > 0 && suspendTime[i] > displayTime[2])
					{
						renderHukidashiChat(writingString[i], i, 1.0F);
					}
					else if(suspendTime[i] > 0)
					{
						renderHukidashiChat(writingString[i], i, (float)suspendTime[i] / displayTime[2]);
					}
				}
			}
		}
	}

	private int[] checkHukidashiView(Minecraft MC, EntityPlayer player, double playerSpace)
	{
		// 画面内に相手プレイヤーがいるかの確認
		
		float viewYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);
		int widthPixel = 0, heightPixel = 0;
		// 右+ : 上+
		
		playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - 10, 2) + Math.pow(MC.thePlayer.posY - 64, 2) + Math.pow(MC.thePlayer.posZ - 10, 2));
		
		boolean returnBoolean = false;
		
		if(viewYaw < 0)
		{
			viewYaw += 180;
		}
		else if(viewYaw > 0)
		{
			viewYaw -= 180;
		}
		
		float playerSpaceYaw = (float)(-Math.atan2(MC.thePlayer.posX - 10, MC.thePlayer.posZ - 10) / Math.PI * 180);
		float playerSpacePitch = (float)(Math.asin(Math.abs(MC.thePlayer.posY - 64) / playerSpace) / Math.PI * 180);
		//System.out.println(viewYaw + " : " + MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationPitch));
		
		
		if(MC.thePlayer.posY > player.posY)
		{
			playerSpacePitch = -playerSpacePitch;
		}
		
		//System.out.println((playerSpacePitch - (widthPerFov * MC.displayHeight)) + " : " + MC.thePlayer.rotationPitch + " : " + (playerSpacePitch + (widthPerFov * MC.displayHeight)));
		
		if(playerSpacePitch - (widthPerFov * MC.displayHeight) < MC.thePlayer.rotationPitch && playerSpacePitch + (widthPerFov * MC.displayHeight) > MC.thePlayer.rotationPitch)
		{
			heightPixel = (int)((playerSpacePitch - MC.thePlayer.rotationPitch) * widthPerFov);
			
			if (viewYaw < playerSpaceYaw + gameSettingFov && viewYaw > playerSpaceYaw - gameSettingFov)
			{
				returnBoolean = true;
				widthPixel = (int)((playerSpaceYaw - viewYaw) * widthPerFov);
			}
			else if(180 < playerSpaceYaw + gameSettingFov && (playerSpaceYaw + gameSettingFov) > viewYaw + 360)
			{
				returnBoolean = true;
				widthPixel = (int)((playerSpaceYaw - viewYaw + 360) * widthPerFov);
			}
			else if(-180 > playerSpaceYaw - gameSettingFov && (playerSpaceYaw - gameSettingFov) < viewYaw - 360)
			{
				returnBoolean = true;
				widthPixel = (int)((playerSpaceYaw - viewYaw - 360) * widthPerFov);
			}
		}
		
		int[] returnInt = {BooleanUtils.toInteger(returnBoolean), widthPixel, heightPixel};
		//int[] returnInt = {BooleanUtils.toInteger(returnBoolean), 0, 0};
		
		return returnInt;
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