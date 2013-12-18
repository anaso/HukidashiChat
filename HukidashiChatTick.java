package anaso.HukidashiChat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumSet;

import javax.annotation.processing.RoundEnvironment;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION;
import org.bouncycastle.util.Arrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL11.*;
import org.objectweb.asm.tree.analysis.Value;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
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
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import cpw.mods.fml.common.*;

public class HukidashiChatTick implements ITickHandler
{
	HashMap <String, Object> Options = new HashMap<String, Object>();

	private final EnumSet<TickType> tickSet = EnumSet.of(TickType.RENDER);

	String[] listenerString = {"",""};  // リスナーから受け取る文字列
	String[] bufferedString = {"",""};  // APIからの文字列のバッファ

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
	int[] hukidashiInGui;  // 吹き出しのGUIへのめり込み数

	//double[][] playerPos = {{0,0},{0,0},{0,0},{0,0}};

	int nameColor = 0;  // 名前の文字色
	int textColor = 0;  // テキストの文字色

	int[] nameColorSplit, textColorSplit;  // テキストの色 Max255

	boolean nameShadow = false;  // 名前に影をつけるか
	boolean textShadow = false;  // テキストに影をつけるか

	int stringColumn = 4;  // テキストの列
	int stringWidth = 83;  // 横の長さ

	int[][] nearCenterPoint = {{0,0},{0,0},{0,0},{0,0}};

	ResourceLocation guiHukidashiMain;
	ResourceLocation guiHukidashi;

	boolean enableAllMessage = false;  //全てのメッセージを表示するか
	boolean viewHukidashi = true;  // 吹き出しを表示するか
	double playerSpaceOption;  // プレイヤー間の距離、コンフィグの値

	int gameSettingFov;
	float widthPerFov;  // 画面1ピクセルあたりの視野角

	// GetChatListenerとの値の受け渡し
	GetChatListener getChatListener;

	// 値をまとめたインスタンスの可変長配列
	ArrayList<HukidashiValues> arrayHukidashiValues = new ArrayList<HukidashiValues>();

	// 削除するインスタンスのオブジェクト保管庫
	ArrayList<HukidashiValues> arrayDeleteValues = new ArrayList<HukidashiValues>();

	public HukidashiChatTick(HashMap Options, GetChatListener getChatListener)
	{
		constructorMethod(Options);
		this.getChatListener = getChatListener;
	}

	public void constructorMethod(HashMap Options)
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
		hukidashiInGui = (int[])Options.get("HukidashiInGui");

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
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{

	}

	public void renderHukidashiMainGUI(String[] writeString, int suspendNumber, float alphaFloat)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		// プレイヤー間の距離
		EntityPlayer player = MC.theWorld.getPlayerEntityByName(writeString[0]);
		double playerSpace = Double.MAX_VALUE;
		if(player != null)
		{
			playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - player.posX, 2) + Math.pow(MC.thePlayer.posY - player.posY, 2) + Math.pow(MC.thePlayer.posZ - player.posZ, 2));
		}

		if(enableAllMessage || playerSpace < playerSpaceOption)  // 全てのメッセージを表示する、もしくは描画距離以内だったとき
		{
			// テクスチャの設定、表示
			GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha * alphaFloat /255);
			MC.getTextureManager().bindTexture(guiHukidashiMain);
			Gui gui = new Gui();
			gui.drawTexturedModalRect(guiPosition[suspendNumber][0], guiPosition[suspendNumber][1], 0, 0, textureSize[0], textureSize[1]);

			int fadeAlpha = (int)(alpha * alphaFloat);

			// フェードイン、フェードアウト用
			int tempNameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);
			int tempTextColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);

			// チャット内容の描画
			MC.fontRenderer.drawString(writeString[0], guiPosition[suspendNumber][0] + textPosition[0], guiPosition[suspendNumber][1] + textPosition[1], tempNameColor, nameShadow);
			for(int i = 1; i < stringColumn; i++)
			{
				if(!writeString[i].equals(""))
				{
					MC.fontRenderer.drawString(writeString[i], guiPosition[suspendNumber][0] + textPosition[2], guiPosition[suspendNumber][1] + textPosition[3] + ((i-1) * MC.fontRenderer.FONT_HEIGHT), tempTextColor, textShadow);
				}
			}

			try
			{

				if(!writeString[0].equals("") && viewHukidashi)
				{
					// 吹き出しの表示処理開始

					//System.out.println(player.worldObj.getWorldInfo().getWorldName() + " : Dim " + player.dimension);

					if(player.worldObj.getWorldInfo().getWorldName().equals(MC.thePlayer.worldObj.getWorldInfo().getWorldName()) && player.dimension == MC.thePlayer.dimension)
					{

						if(playerSpaceOption > playerSpace)
						{
							//System.out.println("in hukidashi");
							int[] hukidashiPixels = checkHukidashiView(MC, player, playerSpace);

							if(hukidashiPixels[0] == 1)
							{
								// 吹き出しの表示
								renderHukidashi(MC, gui, alphaFloat, suspendNumber, hukidashiPixels);
							}
						}
					}
				}
			}

			catch (Exception e)
			{
				if(suspendTime[suspendNumber] % 20 == 0)
				{
					System.out.println(e);
				}
			}
		}

		// System.out.println(MC.theWorld.playerEntities);

		// 16777215 white
		// 65793 black

		suspendTime[suspendNumber]--;
	}

	public void renderHukidashiMainGUI(HukidashiValues hukidashiValues)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		System.out.println("render : " + hukidashiValues.chatString);

		// プレイヤー間の距離
		EntityPlayer player = MC.theWorld.getPlayerEntityByName(hukidashiValues.sendPlayerString);
		double playerSpace = Double.MAX_VALUE;
		if(player != null)
		{
			playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - player.posX, 2) + Math.pow(MC.thePlayer.posY - player.posY, 2) + Math.pow(MC.thePlayer.posZ - player.posZ, 2));
		}

		if(enableAllMessage || playerSpace < playerSpaceOption)  // 全てのメッセージを表示する、もしくは描画距離以内だったとき
		{
			float alphaFloat = 1;

			if(hukidashiValues.fadeinTime > 0)
			{
				alphaFloat = (float)(displayTime[0] - hukidashiValues.fadeinTime / (float)displayTime[0]);
				hukidashiValues.fadeinTime--;
			}
			else if(hukidashiValues.viewTime > 0)
			{
				alphaFloat = 1;
				hukidashiValues.viewTime--;
			}
			else if(hukidashiValues.fadeoutTime > 0)
			{
				alphaFloat = (float)((float)hukidashiValues.fadeoutTime / (float)displayTime[2]);
				hukidashiValues.fadeoutTime--;
			}
			else
			{
				arrayDeleteValues.add(hukidashiValues);
				alphaFloat = 0;
			}

			// テクスチャの描画
			GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)(alpha * alphaFloat /255));
			MC.getTextureManager().bindTexture(guiHukidashiMain);
			Gui gui = new Gui();
			gui.drawTexturedModalRect(hukidashiValues.guiPositionX, hukidashiValues.guiPositionY, 0, 0, textureSize[0], textureSize[1]);

			// 文字の描画
			int fadeAlpha = (int)(alpha * alphaFloat);
			int tempNameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);
			int tempChatColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);

			MC.fontRenderer.drawString(hukidashiValues.nameString, hukidashiValues.guiPositionX + textPosition[0], hukidashiValues.guiPositionY + textPosition[1], tempNameColor, nameShadow);

			/*
			// 複数行の調整 途中
			for(int j = 0; j < stringColumn; j++)
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
			*/
		}
	}

	// 文字列を取得して描画関数を呼び出す
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		if(MC.theWorld != null)
		{
			if(MC.currentScreen == null)
			{
				listenerString = getChatListener.getListenerString();
				String[] resetStrings = {"",""};
				getChatListener.setListenerString(resetStrings);

				if(!listenerString[0].equals(""))
				{
					System.out.println("catch string : " + listenerString[0]);
					int listNumber = checkAvailableNumber(arrayHukidashiValues);

					// インスタンス生成
					HukidashiValues hukidashiValues = new HukidashiValues(listNumber);
					hukidashiValues.setViewTimes(displayTime[0], displayTime[1], displayTime[2]);

					// GUIスケールの倍率の補正
					ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);

					int tempGuiPosition[] = {guiRawPosition[listNumber][0], guiRawPosition[listNumber][1]};

					// 位置の調整
					if(guiRawPosition[listNumber][0] < 0)
					{
						tempGuiPosition[0] = SR.getScaledWidth() + guiRawPosition[listNumber][0] - textureSize[0];
					}

					if(guiRawPosition[listNumber][1] < 0)
					{
						tempGuiPosition[1] = SR.getScaledHeight() + guiRawPosition[listNumber][1] - textureSize[1];
					}
					// GUIの位置の設定
					hukidashiValues.setGuiViewPosition(tempGuiPosition[0], tempGuiPosition[1]);

					hukidashiValues.setHukidashiStrings(listenerString[0], listenerString[1]);

					arrayHukidashiValues.add(hukidashiValues);
				}

				if(arrayHukidashiValues.size() > 0)
				{
					for(HukidashiValues values : arrayHukidashiValues)
					{
						renderHukidashiMainGUI(values);
					}

					checkAndRemoveInstance(arrayHukidashiValues, arrayDeleteValues);
				}

				/*

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

							// ここ以下 多分不要
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
				 */
			}

			/*
				//

				gameSettingFov = 70 + (int)(MC.gameSettings.fovSetting * 40);
				//widthPerFov = ((float)MC.displayWidth / gameSettingFov);
				ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
				widthPerFov = ((float)SR.getScaledWidth() / (float)gameSettingFov);
				// 横1ピクセルあたりの角度

				int[] centerPoint = {SR.getScaledWidth() / 2, SR.getScaledHeight() / 2};

				for(int i = 0; i < 4; i++)
				{
					//System.out.println(suspendTime[i]);
					if(suspendTime[i] > 0)
					{
						int[][] tempPoint = {
								{guiPosition[i][0] + hukidashiInGui[0], guiPosition[i][1] + hukidashiInGui[1]},
								{guiPosition[i][0] + textureSize[0] - hukidashiInGui[0], guiPosition[i][1] + hukidashiInGui[1]},
								{guiPosition[i][0] + hukidashiInGui[0], guiPosition[i][1] + textureSize[1] - hukidashiInGui[1]},
								{guiPosition[i][0] + textureSize[0] - hukidashiInGui[0], guiPosition[i][1] + textureSize[1] - hukidashiInGui[1]}
						};

						// 最短の位置を調べる
						float[] tempCenterPoint = new float[4];
						tempCenterPoint[0] = (float) Math.sqrt(Math.pow((centerPoint[0] - tempPoint[0][0]), 2) + Math.pow((centerPoint[1] - tempPoint[0][1]), 2));
						tempCenterPoint[1] = (float) Math.sqrt(Math.pow((centerPoint[0] - tempPoint[1][0]), 2) + Math.pow((centerPoint[1] - tempPoint[1][1]), 2));
						tempCenterPoint[2] = (float) Math.sqrt(Math.pow((centerPoint[0] - tempPoint[2][0]), 2) + Math.pow((centerPoint[1] - tempPoint[2][1]), 2));
						tempCenterPoint[3] = (float) Math.sqrt(Math.pow((centerPoint[0] - tempPoint[3][0]), 2) + Math.pow((centerPoint[1] - tempPoint[3][1]), 2));

						float tempMin = Float.MAX_VALUE;

						for(int k = 0; k < 4; k++)
						{
							if(tempMin > tempCenterPoint[k])
							{
								tempMin = tempCenterPoint[k];
								nearCenterPoint[i][0] = tempPoint[k][0];
								nearCenterPoint[i][1] = tempPoint[k][1];
							}
						}

						// フェードイン、フェードアウト処理
						if(suspendTime[i] > displayTime[1] + displayTime[2])
						{
							renderHukidashiMainGUI(writingString[i], i, 1.0F - (float)(suspendTime[i] - displayTime[1] - displayTime[2]) / (float)displayTime[0]);
						}
						else if(suspendTime[i] > displayTime[2])
						{
							renderHukidashiMainGUI(writingString[i], i, 1.0F);
						}
						else
						{
							renderHukidashiMainGUI(writingString[i], i, (float)suspendTime[i] / displayTime[2]);
						}
					}
				}
			 */
		}
	}


	private void renderHukidashi(Minecraft MC, Gui gui, float alphaFloat, int suspendNumber, int[] hukidashiPixels)
	{
		ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
		float SRMagnification = MC.displayHeight / SR.getScaledHeight();
		//MC.fontRenderer.drawString("o", ((int)((SR.getScaledWidth() / 2) + (hukidashiPixels[1] / SRMagnification))), ((int)((SR.getScaledHeight() / 2) + (hukidashiPixels[2] / SRMagnification))), 16777215);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha * alphaFloat /255);
		MC.getTextureManager().bindTexture(guiHukidashi);

		GL11.glTranslatef(nearCenterPoint[suspendNumber][0], nearCenterPoint[suspendNumber][1], 0);
		GL11.glRotatef((float) (Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[suspendNumber][1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[suspendNumber][0])) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslatef(-hukidashiRotation[0], -hukidashiRotation[1], 0);
		// GUI位置の調整

		gui.drawTexturedModalRect(0, 0, 0, 0, hukidashiSize[0], hukidashiSize[1]);
		// 吹き出しの描画

		GL11.glTranslatef(hukidashiRotation[0], hukidashiRotation[1], 0);
		GL11.glRotatef((float) -(Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[suspendNumber][1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[suspendNumber][0])) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslatef(-nearCenterPoint[suspendNumber][0], -nearCenterPoint[suspendNumber][1], 0);
		// GUI位置を元に戻す
	}

	private int[] checkHukidashiView(Minecraft MC, EntityPlayer tempPlayer, double tempPlayerSpace)
	{
		// 画面内に相手プレイヤーがいるかの確認

		float viewYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);
		int widthPixel = 0, heightPixel = 0;
		// 右が+、上も+

		tempPlayerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - tempPlayer.posX, 2) + Math.pow(MC.thePlayer.posY - tempPlayer.posY, 2) + Math.pow(MC.thePlayer.posZ - tempPlayer.posZ, 2));
		// プレイヤー間の距離の測定

		boolean returnBoolean = false;

		if(viewYaw < 0)
		{
			viewYaw += 180;
		}
		else if(viewYaw > 0)
		{
			viewYaw -= 180;
		}

		float playerSpaceYaw = (float)(-Math.atan2(MC.thePlayer.posX - tempPlayer.posX, MC.thePlayer.posZ - tempPlayer.posZ) / Math.PI * 180);
		float playerSpacePitch = (float)(Math.asin((MC.thePlayer.posY - tempPlayer.posY - 1) / tempPlayerSpace) / Math.PI * 180);

		if(tempPlayerSpace < playerSpaceOption && tempPlayerSpace > 1)
		{
			if(playerSpacePitch - (widthPerFov * MC.displayHeight) < MC.thePlayer.rotationPitch && playerSpacePitch + (widthPerFov * MC.displayHeight) > MC.thePlayer.rotationPitch && Math.abs(playerSpacePitch) < 75)
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
					widthPixel = (int)((playerSpaceYaw - viewYaw - 360) * widthPerFov);
				}
				else if(-180 > playerSpaceYaw - gameSettingFov && (playerSpaceYaw - gameSettingFov) < viewYaw - 360)
				{
					returnBoolean = true;
					widthPixel = (int)((playerSpaceYaw - viewYaw + 360) * widthPerFov);
				}
			}
		}

		// 実行するか, 横のピクセル, 縦のピクセル
		int[] returnInt = {BooleanUtils.toInteger(returnBoolean), widthPixel, heightPixel};

		return returnInt;
	}

	private int[] checkHukidashiView(Minecraft MC, float[] location, double tempPlayerSpace)
	{
		// 画面内に相手プレイヤーがいるかの確認
		// location [X Y Z] の順;

		float viewYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);
		int widthPixel = 0, heightPixel = 0;
		// 右が+、上も+

		tempPlayerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - location[0], 2) + Math.pow(MC.thePlayer.posY - location[1], 2) + Math.pow(MC.thePlayer.posZ - location[2], 2));
		// プレイヤー間の距離の測定

		boolean returnBoolean = false;

		if(viewYaw < 0)
		{
			viewYaw += 180;
		}
		else if(viewYaw > 0)
		{
			viewYaw -= 180;
		}

		float playerSpaceYaw = (float)(-Math.atan2(MC.thePlayer.posX - location[0], MC.thePlayer.posZ - location[2]) / Math.PI * 180);
		float playerSpacePitch = (float)(Math.asin((MC.thePlayer.posY - location[1] - 1) / tempPlayerSpace) / Math.PI * 180);

		if(tempPlayerSpace < playerSpaceOption && tempPlayerSpace > 1)
		{
			if(playerSpacePitch - (widthPerFov * MC.displayHeight) < MC.thePlayer.rotationPitch && playerSpacePitch + (widthPerFov * MC.displayHeight) > MC.thePlayer.rotationPitch && Math.abs(playerSpacePitch) < 75)
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
					widthPixel = (int)((playerSpaceYaw - viewYaw - 360) * widthPerFov);
				}
				else if(-180 > playerSpaceYaw - gameSettingFov && (playerSpaceYaw - gameSettingFov) < viewYaw - 360)
				{
					returnBoolean = true;
					widthPixel = (int)((playerSpaceYaw - viewYaw + 360) * widthPerFov);
				}
			}
		}

		// 実行するか, 横のピクセル, 縦のピクセル
		int[] returnInt = {BooleanUtils.toInteger(returnBoolean), widthPixel, heightPixel};

		return returnInt;
	}

	boolean checkAndRemoveInstance(ArrayList<HukidashiValues> arrayHukidashiValues, ArrayList<HukidashiValues>arrayDeleteValues)
	{
		boolean returnBoolean = false;

		for(HukidashiValues deleteValues : arrayDeleteValues)
		{
			returnBoolean = arrayHukidashiValues.remove(deleteValues);
			//System.out.println("Removed Object arrayList");
		}

		return returnBoolean;
	}

	int checkAvailableNumber(ArrayList<HukidashiValues> arrayHukidashiValues)
	{
		boolean[] checkList = new boolean[4];
		int returnInt = Integer.MAX_VALUE;

		for(HukidashiValues values : arrayHukidashiValues)
		{
			checkList[values.listNumber] = true;
		}

		for(int i = 0; i < checkList.length; i++)
		{
			if(!checkList[i])
			{
				returnInt = i;
				break;
			}
		}

		return returnInt;
	}

	void bufferedHukidashi(String[] setStrings)
	{
		bufferedString = setStrings;
	}

	String[] getBufferString()
	{
		String[] temp = bufferedString;
		bufferedString[0] = "";
		bufferedString[1] = "";
		return temp;
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