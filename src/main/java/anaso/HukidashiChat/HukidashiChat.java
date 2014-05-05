package anaso.HukidashiChat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.BooleanUtils;

import java.util.HashMap;

@Mod
(
	modid = "HukidashiChat",
	version = "1.0"
)

public class HukidashiChat
{
	@SidedProxy(clientSide = "anaso.HukidashiChat.ClientProxy", serverSide = "anaso.HukidashiChat.CommonProxy")
	public static CommonProxy proxy;

	HashMap <String, Object> Options = new HashMap<String, Object>();

	Configuration cfg;

	String guiPos = "gui_position";
	String textColor = "text_color";

	String[] empty = {""};

	HukidashiChatTick hukidashiChatTick;

	@Mod.EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		cfg = new Configuration(event.getSuggestedConfigurationFile());
		try
		{
			cfg.load();
			Property propDisplayTime  = cfg.get(cfg.CATEGORY_GENERAL, "Display Time", 200, "Value is Tick (1[Tick] = 1/20[Second])");
			Property propFadeInTime = cfg.get(cfg.CATEGORY_GENERAL, "Fade-in Time", 0, "Value is Tick (1[Tick] = 1/20[Second])");
			Property propFadeOutTime = cfg.get(cfg.CATEGORY_GENERAL, "Fade-out Time", 20, "Value is Tick (1[Tick] = 1/20[Second])");
			Property propAlpha = cfg.get(cfg.CATEGORY_GENERAL, "Gui Alpha", 200, "Min = 0, Max = 255");
			Property propReplaceCC = cfg.get(cfg.CATEGORY_GENERAL, "Replace CC", true);

			Property propGuiPos1X = cfg.get(guiPos, "Gui Pos 1 X", 20, "Minus Value is count LowerRight");
			Property propGuiPos1Y = cfg.get(guiPos, "Gui Pos 1 Y", 30, "Minus Value is count LowerRight");
			Property propGuiPos2X = cfg.get(guiPos, "Gui Pos 2 X", -20, "Minus Value is count LowerRight");
			Property propGuiPos2Y = cfg.get(guiPos, "Gui Pos 2 Y", 30, "Minus Value is count LowerRight");

			Property propGuiPos3X = cfg.get(guiPos, "Gui Pos 3 X", 20, "Minus Value is count LowerRight");
			Property propGuiPos3Y = cfg.get(guiPos, "Gui Pos 3 Y", -60, "Minus Value is count LowerRight");
			Property propGuiPos4X = cfg.get(guiPos, "Gui Pos 4 X", -20, "Minus Value is count LowerRight");
			Property propGuiPos4Y = cfg.get(guiPos, "Gui Pos 4 Y", -60, "Minus Value is count LowerRight");

			Property propStringColumn = cfg.get(guiPos, "String Column", 4);
			Property propStringWidth = cfg.get(guiPos, "String Width", 83);

			Property propTextureSizeX = cfg.get(cfg.CATEGORY_GENERAL, "Gui Texture Size X", 100);
			Property propTextureSizeY = cfg.get(cfg.CATEGORY_GENERAL, "Gui Texture Size Y", 55);

			Property propHukidashiSizeX = cfg.get(cfg.CATEGORY_GENERAL, "Hukidashi Texture Size X", 73);
			Property propHukidashiSizeY = cfg.get(cfg.CATEGORY_GENERAL, "Hukidashi Texture Size Y", 14	);

			Property propNamePosX = cfg.get(guiPos, "Player Name Pos X", 9);
			Property propNamePosY = cfg.get(guiPos, "Player Name Pos Y", 5);
			Property propTextPosX = cfg.get(guiPos, "Chat Text Pos X", 10);
			Property propTextPosY = cfg.get(guiPos, "Chat Text Pos Y", 17);

			Property propNameColorR = cfg.get(textColor, "Player Name Color Red", 255, "Min = 0, Max = 255");
			Property propNameColorG = cfg.get(textColor, "Player Name Color Green", 255, "Min = 0, Max = 255");
			Property propNameColorB = cfg.get(textColor, "Player Name Color Blue", 255, "Min = 0, Max = 255");
			Property propNameShadow = cfg.get(textColor, "Player Name Shadow", true);

			Property propTextColorR = cfg.get(textColor, "Chat Text Color Red", 0, "Min = 0, Max = 255");
			Property propTextColorG = cfg.get(textColor, "Chat Text Color Green", 0, "Min = 0, Max = 255");
			Property propTextColorB = cfg.get(textColor, "Chat Text Color Blue", 0, "Min = 0, Max = 255");
			Property propTextShadow = cfg.get(textColor, "Chat Text Shadow", false);

			Property propIncludeMyMessage = cfg.get(cfg.CATEGORY_GENERAL, "Include My Message", false);

			Property propMutePlayer = cfg.get(cfg.CATEGORY_GENERAL, "Mute Player", empty);
			Property propMuteMessage = cfg.get(cfg.CATEGORY_GENERAL, "Mute Message", empty);

			Property propViewAllMessage = cfg.get(cfg.CATEGORY_GENERAL, "View All Message", false);
			Property propViewHukidashi = cfg.get(cfg.CATEGORY_GENERAL, "View Hukidashi", true);
			Property propPlayerSpace = cfg.get(cfg.CATEGORY_GENERAL, "Player Space", 75);



			int[] displayTime = {propFadeInTime.getInt(), propDisplayTime.getInt(), propFadeOutTime.getInt()};
			int alpha = propAlpha.getInt();
			boolean replaceCC = propReplaceCC.getBoolean(true);

			int guiPos1X = propGuiPos1X.getInt();
			int guiPos1Y = propGuiPos1Y.getInt();
			int guiPos2X = propGuiPos2X.getInt();
			int guiPos2Y = propGuiPos2Y.getInt();
			int guiPos3X = propGuiPos3X.getInt();
			int guiPos3Y = propGuiPos3Y.getInt();
			int guiPos4X = propGuiPos4X.getInt();
			int guiPos4Y = propGuiPos4Y.getInt();
			int[][] tempGuiPos = {{guiPos1X,guiPos1Y},{guiPos2X,guiPos2Y},{guiPos3X,guiPos3Y},{guiPos4X,guiPos4Y}};

			int stringColumn = propStringColumn.getInt();
			int stringWidth = propStringWidth.getInt();

			int[] textureSize = {propTextureSizeX.getInt(),propTextureSizeY.getInt()};

			int[] hukidashiSize = {propHukidashiSizeX.getInt(),propHukidashiSizeY.getInt()};

			int[] textPos = {propNamePosX.getInt(), propNamePosY.getInt(), propTextPosX.getInt(), propTextPosY.getInt()};
			// 名前X 名前Y 本文X 本文Y

			int[] nameColor = {propNameColorR.getInt(),propNameColorG.getInt(),propNameColorB.getInt(),BooleanUtils.toInteger(propNameShadow.getBoolean(true))};
			int[] textColor = {propTextColorR.getInt(),propTextColorG.getInt(),propTextColorB.getInt(),BooleanUtils.toInteger(propTextShadow.getBoolean(false))};
			// 赤 青 緑 影

			String includeMyMessage = Boolean.valueOf(propIncludeMyMessage.getBoolean(false)).toString();

			String[] mutePlayer = propMutePlayer.getStringList();
			String[] muteMessage = propMuteMessage.getStringList();

			boolean viewAllMessage = propViewAllMessage.getBoolean(false);
			boolean viewHukidashi = propViewHukidashi.getBoolean(true);
			int playerSpace = propPlayerSpace.getInt();

			Options.put("DisplayTime", displayTime);
			Options.put("Alpha", alpha);
			Options.put("ReplaceCC", Boolean.toString(replaceCC));
			Options.put("GuiPosition", tempGuiPos);
			Options.put("StringColumn", stringColumn);
			Options.put("StringWidth", stringWidth);
			Options.put("TextureSize", textureSize);
			Options.put("HukidashiSize", hukidashiSize);
			Options.put("TextPosition", textPos);
			Options.put("NameColor", nameColor);
			Options.put("TextColor", textColor);
			Options.put("IncludeMyMessage", includeMyMessage);
			Options.put("MutePlayer", mutePlayer);
			Options.put("MuteMessage", muteMessage);
			Options.put("ViewAllMessage", Boolean.toString(viewAllMessage));
			Options.put("ViewHukidashi", Boolean.toString(viewHukidashi));
			Options.put("PlayerSpace", playerSpace);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
		finally
		{
			cfg.save();
		}
	}

	@Mod.EventHandler
	public void Init(FMLInitializationEvent event)
	{
		hukidashiChatTick = new HukidashiChatTick(Options);
		proxy.RegisterTicking(hukidashiChatTick);
	}

	@Mod.EventHandler
	public void PostInit(FMLPostInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new getChat(this));
	}
}
