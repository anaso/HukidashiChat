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
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		cfg = new Configuration(event.getSuggestedConfigurationFile());
		try
		{
			cfg.load();
			Property propDisplayTime  = cfg.get(cfg.CATEGORY_BLOCK, "Display time", 60);

			int displayTime = propDisplayTime.getInt();

			Options.put("DisplayTime", displayTime);
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
		
		File textureFile = new File(ModLoader.getMinecraftInstance().mcDataDir.toString() + "/config/HukidashiChat/hukidashi.png");
		File textureDir = new File(ModLoader.getMinecraftInstance().mcDataDir.toString() + "/config/HukidashiChat");
		
		if(!textureDir.exists())
		{
			textureDir.mkdir();
		}
		else
		{
			if(textureFile.exists())
			{
				Options.put("TextureFilePath", textureFile.toString());
				
				System.out.println("HuidashiChat Load Texture.");
			}
		}
	}
	
	@EventHandler
	public void PostInit(FMLPostInitializationEvent event)
	{
		NetworkRegistry.instance().registerChatListener(new GetChatListener());
	}
}