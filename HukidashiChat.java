package anaso.HukidashiChat;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

import net.minecraft.src.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import org.apache.commons.lang3.BooleanUtils;
import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.util.ResourceLocation;

@Mod
(
	modid = "HukidashiChat",
	name = "Hukidashi Chat",
	version = "1.6"
)

public class HukidashiChat
{
	@SidedProxy(clientSide = "anaso.HukidashiChat.ClientProxy", serverSide = "anaso.HukidashiChat.CommonProxy")
	public static CommonProxy proxy;

	HashMap <String, Object> Options = new HashMap<String, Object>();
	
	Configuration cfg;
	
	String guiPos = "gui_position";
	String textColor = "text_color";
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		cfg = new Configuration(event.getSuggestedConfigurationFile());
		try
		{
			cfg.load();
			Property propDisplayTime  = cfg.get(cfg.CATEGORY_GENERAL, "Display Time", 200, "Value is Tick (1[Tick] = 1/20[Second])");
			Property propAlpha = cfg.get(cfg.CATEGORY_GENERAL, "Gui Alpha", 200, "Min = 0, Max = 255");
			
			Property propGuiPos1X = cfg.get(guiPos, "Gui Pos 1 X", 20, "Minus Value is count LowerRight");
			Property propGuiPos1Y = cfg.get(guiPos, "Gui Pos 1 Y", 30, "Minus Value is count LowerRight");
			Property propGuiPos2X = cfg.get(guiPos, "Gui Pos 2 X", -20, "Minus Value is count LowerRight");
			Property propGuiPos2Y = cfg.get(guiPos, "Gui Pos 2 Y", 30, "Minus Value is count LowerRight");
			
			Property propGuiPos3X = cfg.get(guiPos, "Gui Pos 3 X", 20, "Minus Value is count LowerRight");
			Property propGuiPos3Y = cfg.get(guiPos, "Gui Pos 3 Y", -60, "Minus Value is count LowerRight");
			Property propGuiPos4X = cfg.get(guiPos, "Gui Pos 4 X", -20, "Minus Value is count LowerRight");
			Property propGuiPos4Y = cfg.get(guiPos, "Gui Pos 4 Y", -60, "Minus Value is count LowerRight");
			
			Property propTextureSizeX = cfg.get(cfg.CATEGORY_GENERAL, "Gui Texture Size X", 100);
			Property propTextureSizeY = cfg.get(cfg.CATEGORY_GENERAL, "Gui Texture Size Y", 55);
			
			Property propNamePosX = cfg.get(guiPos, "Player Name Pos X", 9);
			Property propNamePosY = cfg.get(guiPos, "Player Name Pos Y", 5);
			Property propTextPosX = cfg.get(guiPos, "Chat Text Pos X", 10);
			Property propTextPosY = cfg.get(guiPos, "Chat Text Pos Y", 18);
			
			Property propNameColorR = cfg.get(textColor, "Player Name Color Red", 255, "Min = 0, Max = 255");
			Property propNameColorG = cfg.get(textColor, "Player Name Color Green", 255, "Min = 0, Max = 255");
			Property propNameColorB = cfg.get(textColor, "Player Name Color Blue", 255, "Min = 0, Max = 255");
			Property propNameShadow = cfg.get(textColor, "Player Name Shadow", true);
			
			Property propTextColorR = cfg.get(textColor, "Chat Text Color Red", 0, "Min = 0, Max = 255");
			Property propTextColorG = cfg.get(textColor, "Chat Text Color Green", 0, "Min = 0, Max = 255");
			Property propTextColorB = cfg.get(textColor, "Chat Text Color Blue", 0, "Min = 0, Max = 255");
			Property propTextShadow = cfg.get(textColor, "Chat Text Shadow", false);
			
			Property propIncludeMyMessage = cfg.get(cfg.CATEGORY_GENERAL, "Include My Message", false);
			


			int displayTime = propDisplayTime.getInt();
			int alpha = propAlpha.getInt();
			
			int guiPos1X = propGuiPos1X.getInt();
			int guiPos1Y = propGuiPos1Y.getInt();
			int guiPos2X = propGuiPos2X.getInt();
			int guiPos2Y = propGuiPos2Y.getInt();
			int guiPos3X = propGuiPos3X.getInt();
			int guiPos3Y = propGuiPos3Y.getInt();
			int guiPos4X = propGuiPos4X.getInt();
			int guiPos4Y = propGuiPos4Y.getInt();
			int[][] tempGuiPos = {{guiPos1X,guiPos1Y},{guiPos2X,guiPos2Y},{guiPos3X,guiPos3Y},{guiPos4X,guiPos4Y}};
			
			int[] textureSize = {propTextureSizeX.getInt(),propTextureSizeY.getInt()};
			
			int[] textPos = {propNamePosX.getInt(), propNamePosY.getInt(), propTextPosX.getInt(), propTextPosY.getInt()};
			// 名前X 名前Y 本文X 本文Y
			
			int[] nameColor = {propNameColorR.getInt(),propNameColorG.getInt(),propNameColorB.getInt(),BooleanUtils.toInteger(propNameShadow.getBoolean(true))};
			int[] textColor = {propTextColorR.getInt(),propTextColorG.getInt(),propTextColorB.getInt(),BooleanUtils.toInteger(propTextShadow.getBoolean(false))};

			String includeMyMessage = Boolean.valueOf(propIncludeMyMessage.getBoolean(false)).toString();
			
			Options.put("DisplayTime", displayTime);
			Options.put("Alpha", alpha);
			Options.put("GuiPosition", tempGuiPos);
			Options.put("TextureSize", textureSize);
			Options.put("TextPosition", textPos);
			Options.put("NameColor", nameColor);
			Options.put("TextColor", textColor);
			Options.put("IncludeMyMessage", includeMyMessage);
		}
		catch (Exception e)
		{
			FMLLog.log(Level.SEVERE, e, "Error Message");
		}
		finally
		{
			cfg.save();
		}
	}
	
	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		proxy.RegisterTicking(Options);
	}
	
	@EventHandler
	public void PostInit(FMLPostInitializationEvent event)
	{
		NetworkRegistry.instance().registerChatListener(new GetChatListener(Options));
	}
}
