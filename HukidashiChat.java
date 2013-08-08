package anaso.HukidashiChat;

import java.util.HashMap;
import net.minecraft.src.*;
import net.minecraft.world.*;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;

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

	@EventHandler
	public void PostInit(FMLPostInitializationEvent event)
	{
	}

	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		proxy.RegisterTicking(Options);
	}
}