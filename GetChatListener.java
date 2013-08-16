package anaso.HukidashiChat;

import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet3Chat;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.IConnectionHandler;
import com.google.gson.*;

public class GetChatListener implements IChatListener
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
		
		if(messageClass.translate.equals("chat.type.text"))
		{
			//System.out.println("GetUsing : " + messageClass.getUsing()[0]);
			
			HukidashiChatTick.listenerString = messageClass.getUsing();
		}
		return message;
	}
}