package anaso.HukidashiChat;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.BooleanUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;

public class HukidashiChatTick{

	HashMap<String, Object> Options = new HashMap<String, Object>();

	//private final EnumSet<TickType> tickSet = EnumSet.of(TickType.RENDER);

	int alpha = 255;  // 透過率 Max255
	int[] displayTime = {0, 200, 20};  // 初期表示時間
	int[][] guiRawPosition = new int[4][2];  // GUIの位置(コンフィグの値)
	int[] textureSize;  // メインのテクスチャサイズ
	int[] textPosition;  // GUI内のテキストの位置
	int[] hukidashiSize;  // 吹き出しのテクスチャサイズ

	//double[][] playerPos = {{0,0},{0,0},{0,0},{0,0}};

	int nameColor = 0;  // 名前の文字色
	int textColor = 0;  // テキストの文字色

	int[] nameColorSplit, textColorSplit;  // テキストの色 Max255

	boolean nameShadow = false;  // 名前に影をつけるか
	boolean textShadow = false;  // テキストに影をつけるか

	int stringColumn = 4;  // テキストの列
	int stringWidth = 83;  // 横の長さ

	ResourceLocation guiHukidashiMain = new ResourceLocation("hukidashichat:textures/gui/hukidashi_gui.png");
	ResourceLocation guiHukidashi = new ResourceLocation("hukidashichat:/textures/gui/hukidashi.png");


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

	public HukidashiChatTick(HashMap Options){
		constructorMethod(Options);
	}

	public void constructorMethod(HashMap Options){
		this.Options = Options;

		guiRawPosition = (int[][]) Options.get("GuiPosition");

		alpha = (Integer) (Options.get("Alpha"));
		displayTime = (int[]) (Options.get("DisplayTime"));
		textureSize = (int[]) Options.get("TextureSize");
		hukidashiSize = (int[]) Options.get("HukidashiSize");

		textPosition = (int[]) Options.get("TextPosition");
		nameColorSplit = (int[]) Options.get("NameColor");
		textColorSplit = (int[]) Options.get("TextColor");

		stringColumn = (Integer) Options.get("StringColumn");
		stringWidth = (Integer) Options.get("StringWidth");

		if(nameColorSplit[3] != 0){
			nameShadow = true;
		}
		if(textColorSplit[3] != 0){
			textShadow = true;
		}

		nameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((alpha & 255) << 24);
		textColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((alpha & 255) << 24);

		//System.out.println(nameColor + " : " + textColor + " : " + nameColorSplit[3] + " : " + textColorSplit[3]);

		enableAllMessage = Boolean.parseBoolean((String) Options.get("ViewAllMessage"));
		viewHukidashi = Boolean.parseBoolean((String) Options.get("ViewHukidashi"));

		playerSpaceOption = (double) (Integer) Options.get("PlayerSpace");
	}

	public void renderHukidashiMainGUI(HukidashiValues hukidashiValues){
		Minecraft MC = FMLClientHandler.instance().getClient();

		//System.out.println("render : " + hukidashiValues.chatString);

		// プレイヤー間の距離
		double playerSpace = Double.MAX_VALUE;
		double[] targetLocation = {0, 0, 0};

		// 距離の測定
		if(hukidashiValues.isPlayer){
			EntityPlayer player = MC.theWorld.getPlayerEntityByName(hukidashiValues.sendPlayerString);
			if(player != null && !hukidashiValues.sendMeFlag){
				targetLocation[0] = player.posX;
				targetLocation[1] = player.posY;
				targetLocation[2] = player.posZ;
			} else if(player == null && !hukidashiValues.sendMeFlag){
				System.out.println("PlayerNotFound! : " + hukidashiValues.sendPlayerString);
				hukidashiValues.sendMeFlag = true;
			}
		} else{
			targetLocation = hukidashiValues.worldPosition;
		}

		playerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - targetLocation[0], 2) + Math.pow(MC.thePlayer.posY - targetLocation[1], 2) + Math.pow(MC.thePlayer.posZ - targetLocation[2], 2));

		// 全てのメッセージを表示する、描画距離以内、自分で発言したことにするフラグ のどれかがtrue
		if(enableAllMessage || playerSpace < playerSpaceOption || hukidashiValues.sendMeFlag){
			float alphaFloat = 1;

			if(hukidashiValues.fadeinTime > 0){
				alphaFloat = 1F - (hukidashiValues.fadeinTime / (float) displayTime[0]);
				hukidashiValues.fadeinTime--;
			} else if(hukidashiValues.viewTime > 0){
				alphaFloat = 1;
				hukidashiValues.viewTime--;
			} else if(hukidashiValues.fadeoutTime > 0){
				alphaFloat = (float) ((float) hukidashiValues.fadeoutTime / (float) displayTime[2]);
				hukidashiValues.fadeoutTime--;
			} else{
				// インスタンス削除要請を出して終了
				arrayDeleteValues.add(hukidashiValues);
				return;
			}

			// テクスチャの描画
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);//アルファの設定
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, (float) (alpha * alphaFloat / 255));
			MC.getTextureManager().bindTexture(guiHukidashiMain);
			Gui gui = new Gui();
			gui.drawTexturedModalRect(hukidashiValues.guiPositionX, hukidashiValues.guiPositionY, 0, 0, textureSize[0], textureSize[1]);

			// 文字の描画
			int fadeAlpha = (int) (alpha * alphaFloat);
			int tempNameColor = ((nameColorSplit[0] & 255) << 16) + ((nameColorSplit[1] & 255) << 8) + (nameColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);
			int tempChatColor = ((textColorSplit[0] & 255) << 16) + ((textColorSplit[1] & 255) << 8) + (textColorSplit[2] & 255) + ((fadeAlpha & 255) << 24);

			if(fadeAlpha > 5 && fadeAlpha < 250){
				MC.fontRenderer.drawString(hukidashiValues.nameString, hukidashiValues.guiPositionX + textPosition[0], hukidashiValues.guiPositionY + textPosition[1], tempNameColor, nameShadow);

				int count = 0;
				for(String viewStrings : hukidashiValues.trimmedChatStrings){
					if(!viewStrings.equals("") && count < stringColumn){
						MC.fontRenderer.drawString(viewStrings, hukidashiValues.guiPositionX + textPosition[2], hukidashiValues.guiPositionY + textPosition[3] + (count * MC.fontRenderer.FONT_HEIGHT), tempChatColor, textShadow);
					} else if(count >= stringColumn){
						break;
					}
					count++;
				}
			}

			// フキダシの描画
			try{

				if(viewHukidashi && !hukidashiValues.sendMeFlag){
					// 吹き出しの表示処理開始
					if(playerSpaceOption > playerSpace){
						int[] hukidashiPixels = checkHukidashiView(MC, targetLocation, playerSpace);

						if(hukidashiPixels[0] == 1){
							// 吹き出しの表示
							renderHukidashi(hukidashiValues, MC, gui, alphaFloat, hukidashiPixels);
						}
					}
				}
			} catch(Exception e){
				if(!hukidashiValues.sendMeFlag){
					hukidashiValues.sendMeFlag = true;
					System.out.println(e);
				}
			}
		}
	}

	// 文字列を取得して描画関数を呼び出す
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event){
		Minecraft MC = FMLClientHandler.instance().getClient();

		if(MC.theWorld != null){
			int listNumber = checkAvailableNumber(arrayHukidashiValues);

			if(listNumber < allHukidashiCount && arrayBufferHukidashiValues.size() > 0){
				// インスタンス生成
				HukidashiValues hukidashiValues = getBufferHukidashiValues(listNumber);

				hukidashiValues.setViewTimes(displayTime[0], displayTime[1], displayTime[2]);

				// GUIスケールの倍率の補正
				ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);

				int tempGuiPosition[] = {guiRawPosition[listNumber][0], guiRawPosition[listNumber][1]};

				// 位置の調整
				if(guiRawPosition[listNumber][0] < 0){
					tempGuiPosition[0] = SR.getScaledWidth() + guiRawPosition[listNumber][0] - textureSize[0];
				}

				if(guiRawPosition[listNumber][1] < 0){
					tempGuiPosition[1] = SR.getScaledHeight() + guiRawPosition[listNumber][1] - textureSize[1];
				}

				// GUIの位置の設定

				// 1ピクセルあたりの角度の計算
				gameSettingFov = 70 + (int) (MC.gameSettings.fovSetting * 40);
				widthPerFov = ((float) SR.getScaledWidth() / (float) gameSettingFov);

				// ゲーム画面の中央の座標
				centerPoint[0] = SR.getScaledWidth() / 2;
				centerPoint[1] = SR.getScaledHeight() / 2;

				hukidashiValues.setGuiViewPosition(tempGuiPosition[0], tempGuiPosition[1]);

				hukidashiValues.setTrimmedChatStrings(trimChatString(MC, hukidashiValues.chatString));

				arrayHukidashiValues.add(hukidashiValues);
			}
		}

		// 通常時にのみ描画を開始する
		if(MC.currentScreen == null){
			if(arrayHukidashiValues.size() > 0){
				for(HukidashiValues values : arrayHukidashiValues){
					renderHukidashiMainGUI(values);
				}

				checkAndRemoveInstance(arrayHukidashiValues, arrayDeleteValues);
			}
		}
	}

	// フキダシの描画関数
	private void renderHukidashi(HukidashiValues hukidashiValues, Minecraft MC, Gui gui, float alphaFloat, int[] hukidashiPixels){

		ScaledResolution SR = new ScaledResolution(MC.gameSettings, MC.displayWidth, MC.displayHeight);
		float SRMagnification = MC.displayHeight / SR.getScaledHeight();
		//MC.fontRenderer.drawString("o", ((int) ((SR.getScaledWidth() / 2) + (hukidashiPixels[1] / SRMagnification))), ((int) ((SR.getScaledHeight() / 2) + (hukidashiPixels[2] / SRMagnification))), 16777215);

		int[] targetPosInDisplay = {(int) ((SR.getScaledWidth() / 2) + (hukidashiPixels[1] / SRMagnification)), (int) ((SR.getScaledHeight() / 2) + (hukidashiPixels[2] / SRMagnification))};

		GL11.glClearStencil(0);
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		// 以降で描くポリゴンなどは絵として描かない
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);

		// 値はステンシルテストで検査せず(GL_ALWAYS)、そのまま書き込みをする。
		GL11.glStencilFunc(GL11.GL_ALWAYS, 5, ~0);

		// テスト合格でステンシル値を glStencilFunc() の第2引数の値に書き換える(GL_REPLACE)
		// ステンシルバッファがない場合はステンシル値はそのまま（第1引数=GL_KEEP）
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

		// 描画しないフキダシをステンシルバッファに書き込む
		MC.getTextureManager().bindTexture(guiHukidashiMain);
		gui.drawTexturedModalRect(hukidashiValues.guiPositionX, hukidashiValues.guiPositionY, 0, 0, textureSize[0], textureSize[1]);

		// 設定を絵の描画用に戻す。
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);// 以降はステンシル値は変更しない。

		GL11.glStencilFunc(GL11.GL_NOTEQUAL, 5, ~0);

		// 透過を有効にしておく
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);//アルファの設定
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, (float) alpha * alphaFloat / 255);
		MC.getTextureManager().bindTexture(guiHukidashi);

		float hukidashiScale = (float) (Math.sqrt(Math.pow(targetPosInDisplay[0] - (hukidashiValues.guiPositionX + (textureSize[0] / 2)), 2) + Math.pow(targetPosInDisplay[1] - (hukidashiValues.guiPositionY + (textureSize[1] / 2)), 2))) / hukidashiSize[0];
		if(hukidashiScale > 1.5F){
			hukidashiScale = 1.5F;
		}

		// GUI位置の調整
		//GL11.glTranslatef(nearCenterPoint[0], nearCenterPoint[1], 0);
		//GL11.glRotatef((float) (Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[0])) / Math.PI * 180), 0, 0, 1.0F);
		//GL11.glTranslatef(-hukidashiRotation[0], -hukidashiRotation[1], 0);

		GL11.glTranslated(hukidashiValues.guiPositionX + (textureSize[0] / 2), hukidashiValues.guiPositionY + (textureSize[1] / 2), 0);
		GL11.glRotatef((float) (Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - (hukidashiValues.guiPositionY + (textureSize[1] / 2))), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - (hukidashiValues.guiPositionX + (textureSize[0] / 2)))) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslatef(-hukidashiSize[1] / 2, -hukidashiSize[1] / 2, 0);

		GL11.glScalef(hukidashiScale, hukidashiScale, 1F);

		// 吹き出しの描画
		gui.drawTexturedModalRect(0, 0, 0, 0, hukidashiSize[0], hukidashiSize[1]);

		// GUI位置の調整を戻す
		//GL11.glTranslatef(hukidashiRotation[0], hukidashiRotation[1], 0);
		//GL11.glRotatef((float) -(Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - nearCenterPoint[1]), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - nearCenterPoint[0])) / Math.PI * 180), 0, 0, 1.0F);
		//GL11.glTranslatef(-nearCenterPoint[0], -nearCenterPoint[1], 0);

		GL11.glScalef(1/hukidashiScale, 1/hukidashiScale, 1F);

		GL11.glTranslatef(hukidashiSize[1] / 2, hukidashiSize[1] / 2, 0);
		GL11.glRotatef((float) -(Math.atan2((((SR.getScaledHeight_double() / 2) + (hukidashiPixels[2] / SRMagnification)) - (hukidashiValues.guiPositionY + (textureSize[1] / 2))), (((SR.getScaledWidth_double() / 2) + (hukidashiPixels[1] / SRMagnification)) - (hukidashiValues.guiPositionX + (textureSize[0] / 2)))) / Math.PI * 180), 0, 0, 1.0F);
		GL11.glTranslated(-(hukidashiValues.guiPositionX + (textureSize[0] / 2)), -(hukidashiValues.guiPositionY + (textureSize[1] / 2)), 0);

		// ステンシルを解除しておく
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	private int[] checkHukidashiView(Minecraft MC, double[] location, double tempPlayerSpace){
		// 画面内に相手プレイヤーがいるかの確認
		// location [X Y Z] の順;

		float viewYaw = MathHelper.wrapAngleTo180_float(MC.thePlayer.rotationYaw);
		int widthPixel = 0, heightPixel = 0;
		// 右が+、上も+

		tempPlayerSpace = Math.sqrt(Math.pow(MC.thePlayer.posX - location[0], 2) + Math.pow(MC.thePlayer.posY - location[1], 2) + Math.pow(MC.thePlayer.posZ - location[2], 2));
		// プレイヤー間の距離の測定

		boolean returnBoolean = false;

		if(viewYaw < 0){
			viewYaw += 180;
		} else if(viewYaw > 0){
			viewYaw -= 180;
		}

		float playerSpaceYaw = (float) (-Math.atan2(MC.thePlayer.posX - location[0], MC.thePlayer.posZ - location[2]) / Math.PI * 180);
		float playerSpacePitch = (float) (Math.asin((MC.thePlayer.posY - location[1]) / tempPlayerSpace) / Math.PI * 180);

		if(tempPlayerSpace < playerSpaceOption && tempPlayerSpace > 0.5){
			if(playerSpacePitch - (widthPerFov * MC.displayHeight) < MC.thePlayer.rotationPitch && playerSpacePitch + (widthPerFov * MC.displayHeight) > MC.thePlayer.rotationPitch && Math.abs(playerSpacePitch) < 75){
				heightPixel = (int) ((playerSpacePitch - MC.thePlayer.rotationPitch) * widthPerFov);

				if(viewYaw < playerSpaceYaw + gameSettingFov && viewYaw > playerSpaceYaw - gameSettingFov){
					returnBoolean = true;
					widthPixel = (int) ((playerSpaceYaw - viewYaw) * widthPerFov);
				} else if(180 < playerSpaceYaw + gameSettingFov && (playerSpaceYaw + gameSettingFov) > viewYaw + 360){
					returnBoolean = true;
					widthPixel = (int) ((playerSpaceYaw - viewYaw - 360) * widthPerFov);
				} else if(-180 > playerSpaceYaw - gameSettingFov && (playerSpaceYaw - gameSettingFov) < viewYaw - 360){
					returnBoolean = true;
					widthPixel = (int) ((playerSpaceYaw - viewYaw + 360) * widthPerFov);
				}
			}
		}

		// 実行するか, 横のピクセル, 縦のピクセル
		int[] returnInt = {BooleanUtils.toInteger(returnBoolean), widthPixel, heightPixel};

		return returnInt;
	}

	String[] trimChatString(Minecraft MC, String chatString){
		ArrayList<String> trimmingString = new ArrayList<String>();
		trimmingString.add(chatString);

		// 複数行の調整 途中
		for(int count = 0; true; count++){
			if(trimmingString.size() > count){
				// 改行が必要な長さ以上なら
				if(MC.fontRenderer.getStringWidth(trimmingString.get(count)) > stringWidth){
					// トリミング
					String trimString = MC.fontRenderer.trimStringToWidth(trimmingString.get(count), stringWidth);

					// 次への持ち越し
					if(trimString.length() > 0){
						trimmingString.add(trimmingString.get(count).substring(trimString.length()));
					}

					// 今の値をトリミング後に置き換える
					trimmingString.set(count, trimString);
				} else{
					// 改行が必要ないなら終了
					break;
				}
			} else{
				break;
			}
		}

		return (String[]) trimmingString.toArray(new String[trimmingString.size()]);
	}

	boolean checkAndRemoveInstance(ArrayList<HukidashiValues> arrayHukidashiValues, ArrayList<HukidashiValues> arrayDeleteValues){
		boolean returnBoolean = false;

		for(HukidashiValues deleteValues : arrayDeleteValues){
			returnBoolean = arrayHukidashiValues.remove(deleteValues);
		}

		return returnBoolean;
	}

	int checkAvailableNumber(ArrayList<HukidashiValues> arrayHukidashiValues){
		boolean[] checkList = new boolean[4];
		int returnInt = Integer.MAX_VALUE;

		for(HukidashiValues values : arrayHukidashiValues){
			checkList[values.listNumber] = true;
		}

		for(int i = 0; i < checkList.length; i++){
			if(!checkList[i]){
				returnInt = i;
				break;
			}
		}

		return returnInt;
	}

	void setBufferedHukidashiValues(String[] setStrings, boolean sendMeFlag){
		HukidashiValues hukidashiValues = new HukidashiValues(-1);

		hukidashiValues.setHukidashiStrings(setStrings[0], setStrings[1]);
		hukidashiValues.sendMeFlag = sendMeFlag;

		setBufferedHukidashiValues(hukidashiValues);
	}

	// どこかの座標で発生したとき
	void setBufferedHukidashiValues(String[] setStrings, double posX, double posY, double posZ){
		HukidashiValues hukidashiValues = new HukidashiValues(-1);
		double[] worldPosition = {posX, posY, posZ};

		hukidashiValues.setHukidashiStrings(setStrings[0], setStrings[1]);
		hukidashiValues.setWorldPosition(worldPosition);

		setBufferedHukidashiValues(hukidashiValues);
	}

	void setBufferedHukidashiValues(String nameStrings, String[] chatStrings, double posX, double posY, double posZ){
		HukidashiValues hukidashiValues = new HukidashiValues(-1);
		double[] worldPosition = {posX, posY, posZ};

		hukidashiValues.setHukidashiStrings(nameStrings, chatStrings[0]);
		hukidashiValues.setTrimmedChatStrings(chatStrings);
		hukidashiValues.setWorldPosition(worldPosition);

		setBufferedHukidashiValues(hukidashiValues);
	}

	// 配列に追加
	void setBufferedHukidashiValues(HukidashiValues hukidashiValues){
		if(arrayBufferHukidashiValues.size() < 2){
			arrayBufferHukidashiValues.add(hukidashiValues);
		}
	}

	HukidashiValues getBufferHukidashiValues(int listNumber){
		HukidashiValues returnHukidashiValues = null;

		if(arrayBufferHukidashiValues.size() >= 1){
			returnHukidashiValues = arrayBufferHukidashiValues.get(0);

			returnHukidashiValues.listNumber = listNumber;

			arrayBufferHukidashiValues.remove(0);
		}

		return returnHukidashiValues;
	}
}