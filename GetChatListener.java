package anaso.HukidashiChat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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

	String[] mutePlayer, muteMessage;

	boolean enableMutePlayer = false, enableMuteMessage = false;
	boolean replaceCC = true;

	String ColorCodeString = "§[0-9a-fA-F]";
	Pattern ColorCodePattern = Pattern.compile(ColorCodeString);

	HashMap Options;

	GetChatListener(HashMap Options)
	{
		this.Options = Options;

		mutePlayer = (String[])Options.get("MutePlayer");
		if(mutePlayer != null)
		{
			enableMutePlayer = true;
		}

		muteMessage = (String[])Options.get("MuteMessage");
		if(muteMessage != null)
		{
			enableMuteMessage = true;
		}

		replaceCC = Boolean.parseBoolean((String)Options.get("ReplaceCC"));
	}

	@Override
	public Packet3Chat serverChat(NetHandler handler, Packet3Chat message) {
		return message;
	}

	@Override
	public Packet3Chat clientChat(NetHandler handler, Packet3Chat message) {
		//System.out.println("GetChatMessage : " + message.message);

		messageClass = gson.fromJson(message.message, MessageClass.class);

		if(messageClass != null)
		{
			Minecraft MC = ModLoader.getMinecraftInstance();

			//HukidashiChatTick.listenerString = getMessage(MC);

			String[] getMessage = getMessage(MC);
			if(enableMutePlayer || enableMuteMessage)
			{
				for(String mutePlayerString:mutePlayer)
				{
					if(getMessage[0].equals(mutePlayerString) && !mutePlayerString.equals(""))
					{
						getMessage[0] = "";
						getMessage[1] = "";
						System.out.println("MutePlayer!");
						break;
					}
				}

				for(String muteMessageString:muteMessage)
				{
					if(getMessage[1].indexOf(muteMessageString) > -1 && !muteMessageString.equals(""))
					{
						getMessage[0] = "";
						getMessage[1] = "";
						System.out.println("MuteMessage! :" + muteMessageString + "[END]");
						break;
					}
				}
			}

			if(replaceCC)
			{
				System.out.println(getMessage[1]);
				Matcher matcher = ColorCodePattern.matcher(getMessage[0]);
				getMessage[0] = matcher.replaceAll("");
				matcher = ColorCodePattern.matcher(getMessage[1]);
				getMessage[1] = matcher.replaceAll("");
				System.out.println("Raplace : " + getMessage[1]);
			}

			HukidashiChatTick.listenerString = getMessage;
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

	public String[] getMessage(Minecraft MC)
	{
		// メッセージの取得
		String[] tempReturn = {"",""};

		if(messageClass.getTranslate().equals("chat.type.text"))
		{
			// 公式サーバー用
			try
			{
				//System.out.println("Get : " + Boolean.parseBoolean((String)Options.get("IncludeMyMessage")));


				if(!MC.thePlayer.username.equals(messageClass.getUsing()[0]))
				{
					tempReturn = messageClass.getUsing();
				}
				else if(Boolean.valueOf((String)Options.get("IncludeMyMessage")).booleanValue())
				{
					tempReturn = messageClass.getUsing();
				}

				//System.out.println(MC.theWorld.getPlayerEntityByName(messageClass.getUsing()[0]));
			}
			catch(Exception e)  //エラー吐いたら無かったことに
			{
				System.out.println(e);

				tempReturn[1] = "";
				tempReturn[0] = "";
			}
		}

		if(!messageClass.getText().equals(""))
		{
			// Bukkit用
			try
			{
				int start = -1, end = -1;
				start = messageClass.getText().indexOf("\u003c");
				end = messageClass.getText().indexOf("\u003e");

				//System.out.println("in  " + start + " : " + end);

				if(start >= 0 && end >= 0 && start < end)
				{
					if(!MC.thePlayer.username.equals(messageClass.getText().substring(start + 1, end)))
					{
						tempReturn[1] = messageClass.getText().substring(end + 2);
						tempReturn[0] = messageClass.getText().substring(start + 1, end);
					}
					else if(Boolean.valueOf((String)Options.get("IncludeMyMessage")).booleanValue())
					{
						tempReturn[1] = messageClass.getText().substring(end + 2);
						tempReturn[0] = messageClass.getText().substring(start + 1, end);
					}
				}
			}
			catch(Exception e)  //エラー吐いたら無かったことに
			{
				System.out.println(e);

				tempReturn[1] = "";
				tempReturn[0] = "";
			}
		}

		return tempReturn;
	}
}