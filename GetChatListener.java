package anaso.HukidashiChat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

import com.google.gson.Gson;

public class GetChatListener implements IChatListener, IConnectionHandler
{
	// 参考 https://github.com/jakenjarvis/ChatLoggerPlus/blob/master/src/com/tojc/minecraft/mod/ChatLogger/HandlerAndEventListener.java
	
	MessageClass messageClass;
	Gson gson = new Gson();

	@Override
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
		return message;
	}

	@Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message) {
		System.out.println("GetChatMessage : " + message.message);
		
		messageClass = gson.fromJson(message.message, MessageClass.class);
		
		if(messageClass != null)
		{
			Minecraft MC = ModLoader.getMinecraftInstance();
			
			if(messageClass.getTranslate().equals("chat.type.text"))
			{
				//System.out.println("GetUsing : " + messageClass.getUsing()[0]);
				
				if(!MC.thePlayer.username.equals(messageClass.getUsing()[0]))
				{
					HukidashiChatTick.listenerString = messageClass.getUsing();
				}
			}
			
			if(!messageClass.getText().equals(""))
			{
				int start = -1, end = -1;
				start = messageClass.getText().indexOf("\u003c");
				end = messageClass.getText().indexOf("\u003e");
				
				//System.out.println("in  " + start + " : " + end);
				
				if(start >= 0 && end >= 0 && start < end)
				{
					if(!MC.thePlayer.username.equals(messageClass.getText().substring(start + 1, end)))
					{
						HukidashiChatTick.listenerString[1] = messageClass.getText().substring(end + 2);
						HukidashiChatTick.listenerString[0] = messageClass.getText().substring(start + 1, end);
					}
				}
			}
		}
		
		return message;
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler,
			INetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			INetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, INetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, INetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void connectionClosed(INetworkManager manager) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
}