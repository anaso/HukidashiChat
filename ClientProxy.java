package anaso.HukidashiChat;

import java.util.HashMap;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.src.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void RegisterTicking(HashMap Options)
	{
		TickRegistry.registerTickHandler(new HukidashiChatTick(Options), Side.CLIENT);
	}
}