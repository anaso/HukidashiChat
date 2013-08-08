package anaso.HukidashiChat;

import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet3Chat;
import cpw.mods.fml.common.network.IChatListener;
import cpw.mods.fml.common.network.IConnectionHandler;

public class GetChatListener implements IChatListener
{
	// 参考 https://github.com/jakenjarvis/ChatLoggerPlus/blob/master/src/com/tojc/minecraft/mod/ChatLogger/HandlerAndEventListener.java

	@Override
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}