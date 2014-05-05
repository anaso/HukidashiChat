package anaso.HukidashiChat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void RegisterTicking(HukidashiChatTick hukidashiChatTick)
	{
		FMLCommonHandler.instance().bus().register(hukidashiChatTick);
		new HukidashiAPI(hukidashiChatTick);
	}
}