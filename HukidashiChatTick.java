package anaso.HukidashiChat;

import java.lang.reflect.Array;
import java.sql.Time;
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

	ResourceLocation guiHukidashiMain;
	ResourceLocation guiHukidashi;

	boolean enableAllMessage = false;  //全てのメッセージを表示するか
	boolean viewHukidashi = true;  // 吹き出しを表示するか
	double playerSpaceOption;  // プレイヤー間の距離、コンフィグの値

	int gameSettingFov;
	float widthPerFov;  // 画面1ピクセルあたりの視野角

	int allHukidashiCount = 4;

	// 値をまとめたインスタンスの可変長配列
	ArrayList<HukidashiValues> arrayHukidashiValues = new ArrayList<HukidashiValues>();

	// 削除するインスタンスのオブジェクト保管庫
	ArrayList<HukidashiValues> arrayDeleteValues = new ArrayList<HukidashiValues>();

	// 受け取った文字列のバッファ
	ArrayList<HukidashiValues> arrayBufferHukidashiValues = new ArrayList<HukidashiValues>();

	int[] centerPoint = {0, 0};

	public HukidashiChatTick(HashMap Options)
	{
		constructorMethod(Options);
	}

	public void constructorMethod(HashMap Options)
	{
		this.Options = Options;

		guiRawPosition = (int[][])Options.get("GuiPosition");

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

	public void renderHukidashiMainGUI(HukidashiValues hukidashiValues)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		//System.out.println("render : " + hukidashiValues.chatString);

		// プレイヤー間の距離
		double playerSpace = Double.MAX_VALUE;
		double[] targetLocation = {0,0,0};

		// 距離の測定
		if(hukidashiValues.isPlayer)
		{
			EntityPlayer player = MC.theWorld.getPlayerEntityByName(hukidashiValues.sendPlayerString);
			if(player != null && !hukidashiValues.sendMeFlag)
			{
				targetLocation[0] = player.posX;
				targetLocation[1] = player.posY;
				targetLocation[2] = player.posZ;
			}
			else if(player == null)
			{
				System.out.println("PlayerNotFound! : " + hukidashiValues.sendPlayerString);
				hukidashiValues.sendMeFlag = true;
			}
		}
		else
		{
			targetLocation = hukidashiValues.worldPosition;
		}

		playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - targetLocation[0], 2) + Math.pow(MC.thePlayer.posY - targetLocation[1], 2) + Math.pow(MC.thePlayer.posZ - targetLocation[2], 2));

		// 全てのメッセージを表示する、描画距離以内、自分で発言したことにするフラグ のどれかがtrue
		if(enableAllMessage || playerSpace < playerSpaceOption || hukidashiValues.sendMeFlag)
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
				// インスタンス削除要請を出して終了
				arrayDeleteValues.add(hukidashiValues);
				return;
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

			int count = 0;
			for(String viewStrings : hukidashiValues.trimmedChatStrings)
			{
				if(!viewStrings.equals("") && count < stringColumn && fadeAlpha != 0)
				{
					MC.fontRenderer.drawString(viewStrings, hukidashiValues.guiPositionX + textPosition[2], hukidashiValues.guiPositionY + textPosition[3] + (count * MC.fontRenderer.FONT_HEIGHT), tempChatColor, textShadow);
				}
				else if(count >= stringColumn)
				{
					break;
				}
				count++;
			}

			// フキダシの描画
			try
			{

				if(viewHukidashi && !hukidashiValues.sendMeFlag)
				{
					// 吹き出しの表示処理開始
					if(playerSpaceOption > playerSpace)
					{
						int[] hukidashiPixels = checkHukidashiView(MC, targetLocation, playerSpace);

						if(hukidashiPixels[0] == 1)
						{
							// 吹き出しの表示
							renderHukidashi(hukidashiValues, MC, gui, alphaFloat, hukidashiPixels);
						}
					}
				}
			}

			catch (Exception e)
			{
				if(!hukidashiValues.sendMeFlag)
				{
					hukidashiValues.sendMeFlag = true;
					System.out.println(e);
				}
			}
		}
	}

	// 文字列を取得して描画関数を呼び出す
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Minecraft MC = ModLoader.getMinecraftInstance();

		if(MC.theWorld != null)
		{
			int listNumber = checkAvailableNumber(arrayHukidashiValues);

			if(listNumber < allHukidashiCount && arrayBufferHukidashiValues.size() > 0)
			{
				// インスタンス生成
				HukidashiValues hukidashiValues = getBufferHukidashiValues(listNumber);

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

				// 1ピクセルあたりの角度の計算
				gameSettingFov = 70 + (int)(MC.gameSettings.fovSetting * 40);
				widthPerFov = ((float)SR.getScaledWidth() / (float)gameSettingFov);

				// ゲーム画面の中央の座標
				centerPoint[0] = SR.getScaledWidth() / 2;
				centerPoint[1] = SR.getScaledHeight() / 2;

				hukidashiValues.setGuiViewPosition(tempGuiPosition[0], tempGuiPosition[1]);

				hukidashiValues.setGuiNearPoint(getGuiNearPoints(tempGuiPosition[0], tempGuiPosition[1]));

				hukidashiValues.setTrimmedChatStrings(trimChatString(MC, hukidashiValues.chatString));

				arrayHukidashiValues.add(hukidashiValues);
			}
		}

		// 通常時にのみ描画を開始する
		if(MC.currentScreen == null)
		{
			if(arrayHukidashiValues.size() > 0)
			{
				for(HukidashiValues values : arrayHukidashiValues)
				{
					renderHukidashiMainGUI(values);
				}

				checkAndRemoveInstance(arrayHukidashiValues, arrayDeleteValues);
			}
		}
	}


	private void renderHukidashi(HukidashiValues hukidashiValues, Minecraft MC, Gui gui, float alphaFloat, int[] hukidashiPixels)
	{
		int[] nearCenterPoint = {hukidashiValues.guiNearPointX, hukidashiValues.guiNearPointY};

		ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
		float SRMagnification = MC.displayHeight / SR.getScaledHeight();
		//MC.fontRenderer.drawString("o", ((int)((SR.getScaledWidth() / 2) + (hukidashiPixels[1] / SRMagnification))), ((int)((SR.getScaledHeight() / 2) + (hukidashiPixels[2] / SRMagnification))), 16777215);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)alpha * alphaFloat /255);
		MC.getTextureManager().bindTexture(guiHukidashi);

		GL11.glTranslatef(nearCenterPoint[0], nearCenterPoint[1], 0);
		GL11.glRotatef((float) (Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[0])) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslatef(-hukidashiRotation[0], -hukidashiRotation[1], 0);
		// GUI位置の調整

		gui.drawTexturedModalRect(0, 0, 0, 0, hukidashiSize[0], hukidashiSize[1]);
		// 吹き出しの描画

		GL11.glTranslatef(hukidashiRotation[0], hukidashiRotation[1], 0);
		GL11.glRotatef((float) -(Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[0])) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslatef(-nearCenterPoint[0], -nearCenterPoint[1], 0);
		// GUI位置を元に戻す
	}

	int[] getGuiNearPoints(int guiPositionX, int guiPositionY)
	{
		int[] returnInt = {0, 0};

		// GUIの四隅の座標の取得
		int[][] tempPoint =
			{
				{guiPositionX + hukidashiInGui[0], guiPositionY + hukidashiInGui[1]},
				{guiPositionX + textureSize[0] - hukidashiInGui[0], guiPositionY + hukidashiInGui[1]},
				{guiPositionX + hukidashiInGui[0], guiPositionY + textureSize[1] - hukidashiInGui[1]},
				{guiPositionX + textureSize[0] - hukidashiInGui[0], guiPositionY + textureSize[1] - hukidashiInGui[1]}
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
				returnInt[0] = tempPoint[k][0];
				returnInt[1] = tempPoint[k][1];
			}
		}

		return returnInt;
	}

	private int[] checkHukidashiView(Minecraft MC, double[] location, double tempPlayerSpace)
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
		float playerSpacePitch = (float)(Math.asin((MC.thePlayer.posY - location[1]) / tempPlayerSpace) / Math.PI * 180);

		if(tempPlayerSpace < playerSpaceOption && tempPlayerSpace > 0.5)
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

	String[] trimChatString(Minecraft MC, String chatString)
	{
		ArrayList<String> trimmingString = new ArrayList<String>();
		trimmingString.add(chatString);

		// 複数行の調整 途中
		for(int count = 0; true; count++)
		{
			if(trimmingString.size() > count)
			{
				// 改行が必要な長さ以上なら
				if(MC.fontRenderer.getStringWidth(trimmingString.get(count)) > stringWidth)
				{
					// トリミング
					String trimString = MC.fontRenderer.trimStringToWidth(trimmingString.get(count), stringWidth);

					// 次への持ち越し
					if(trimString.length() > 0)
					{
						trimmingString.add(trimmingString.get(count).substring(trimString.length()));
					}

					// 今の値をトリミング後に置き換える
					trimmingString.set(count, trimString);
				}
				else
				{
					// 改行が必要ないなら終了
					break;
				}
			}
			else
			{
				break;
			}
		}

		return (String[])trimmingString.toArray(new String[trimmingString.size()]);
	}

	boolean checkAndRemoveInstance(ArrayList<HukidashiValues> arrayHukidashiValues, ArrayList<HukidashiValues>arrayDeleteValues)
	{
		boolean returnBoolean = false;

		for(HukidashiValues deleteValues : arrayDeleteValues)
		{
			returnBoolean = arrayHukidashiValues.remove(deleteValues);
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

	void setBufferedHukidashiValues(String[] setStrings, boolean sendMeFlag)
	{
		HukidashiValues hukidashiValues = new HukidashiValues(-1);

		hukidashiValues.setHukidashiStrings(setStrings[0], setStrings[1]);
		hukidashiValues.sendMeFlag = sendMeFlag;

		setBufferedHukidashiValues(hukidashiValues);
	}

	// どこかの座標で発生したとき
	void setBufferedHukidashiValues(String[] setStrings, double posX, double posY, double posZ)
	{
		HukidashiValues hukidashiValues = new HukidashiValues(-1);
		double[] worldPosition = {posX, posY, posZ};

		hukidashiValues.setHukidashiStrings(setStrings[0], setStrings[1]);
		hukidashiValues.setWorldPosition(worldPosition);

		setBufferedHukidashiValues(hukidashiValues);
	}

	void setBufferedHukidashiValues(String nameStrings, String[] chatStrings, double posX, double posY, double posZ)
	{
		HukidashiValues hukidashiValues = new HukidashiValues(-1);
		double[] worldPosition = {posX, posY, posZ};

		hukidashiValues.setHukidashiStrings(nameStrings, chatStrings[0]);
		hukidashiValues.setTrimmedChatStrings(chatStrings);
		hukidashiValues.setWorldPosition(worldPosition);

		setBufferedHukidashiValues(hukidashiValues);
	}

	// 配列に追加
	void setBufferedHukidashiValues(HukidashiValues hukidashiValues)
	{
		if(arrayBufferHukidashiValues.size() < 2)
		{
			arrayBufferHukidashiValues.add(hukidashiValues);
		}
	}

	HukidashiValues getBufferHukidashiValues(int listNumber)
	{
		HukidashiValues returnHukidashiValues = null;

		if(arrayBufferHukidashiValues.size() >= 1)
		{
			returnHukidashiValues = arrayBufferHukidashiValues.get(0);

			returnHukidashiValues.listNumber = listNumber;

			arrayBufferHukidashiValues.remove(0);
		}

		return returnHukidashiValues;
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