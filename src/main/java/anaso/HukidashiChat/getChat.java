package anaso.HukidashiChat;

import com.google.gson.Gson;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getChat{
	// 参考 https://github.com/jakenjarvis/ChatLoggerPlus/blob/master/src/com/tojc/minecraft/mod/ChatLogger/HandlerAndEventListener.java

	String chatMessage;

	String[] mutePlayer, muteMessage;

	boolean enableMutePlayer = false, enableMuteMessage = false;
	boolean replaceCC = true;

	String ColorCodeString = "§[0-9a-fA-F]";
	Pattern ColorCodePattern = Pattern.compile(ColorCodeString);

	HashMap Options;

	HukidashiChatTick hukidashiChatTick;

	getChat(HukidashiChat hukidashiChat){
		this.Options = hukidashiChat.Options;
		this.hukidashiChatTick = hukidashiChat.hukidashiChatTick;

		mutePlayer = (String[]) Options.get("MutePlayer");
		if(mutePlayer != null){
			enableMutePlayer = true;
		}

		muteMessage = (String[]) Options.get("MuteMessage");
		if(muteMessage != null){
			enableMuteMessage = true;
		}

		replaceCC = Boolean.parseBoolean((String) Options.get("ReplaceCC"));
	}

	// チャットを受け取るイベント
	@SubscribeEvent
	public void onChatMessageReceived(ClientChatReceivedEvent event){

		System.out.println("HC : " + event.message.getUnformattedText());

		chatMessage = event.message.getUnformattedText();

		// ミュート処理
		String[] getMessage = getMessage(FMLClientHandler.instance().getClient());

		if(enableMutePlayer || enableMuteMessage){
			for(String mutePlayerString : mutePlayer){
				if(getMessage[0].equals(mutePlayerString) && !mutePlayerString.equals("")){
					getMessage[0] = "";
					getMessage[1] = "";
					System.out.println("MutePlayer!");
					break;
				}
			}

			for(String muteMessageString : muteMessage){
				if(getMessage[1].indexOf(muteMessageString) > -1 && !muteMessageString.equals("")){
					getMessage[0] = "";
					getMessage[1] = "";
					System.out.println("MuteMessage! :" + muteMessageString + "[END]");
					break;
				}
			}
		}

		// カラーコードを削除
		if(replaceCC){
			Matcher matcher = ColorCodePattern.matcher(getMessage[0]);
			getMessage[0] = matcher.replaceAll("");
			matcher = ColorCodePattern.matcher(getMessage[1]);
			getMessage[1] = matcher.replaceAll("");
		}

		if(!getMessage[0].equals("") && !getMessage[1].equals("")){
			hukidashiChatTick.setBufferedHukidashiValues(getMessage, getMessage[0].equals(FMLClientHandler.instance().getClient().thePlayer.getDisplayName()));
		}
	}


	public String[] getMessage(Minecraft MC){
		// メッセージの取得

		String[] tempReturn = {"", ""};

		try{
			int start = -1, end = -1;
			start = chatMessage.indexOf("\u003c");
			end = chatMessage.indexOf("\u003e");

			if(start >= 0 && end >= 0 && start < end){
				if(!MC.thePlayer.getDisplayName().equals(chatMessage.substring(start + 1, end))){
					tempReturn[1] = chatMessage.substring(end + 2);
					tempReturn[0] = chatMessage.substring(start + 1, end);
				} else if(Boolean.valueOf((String) Options.get("IncludeMyMessage")).booleanValue()){
					tempReturn[1] = chatMessage.substring(end + 2);
					tempReturn[0] = chatMessage.substring(start + 1, end);
				}
			}
		} catch(Exception e)  //エラー吐いたら無かったことに
		{
			System.out.println(e);

			tempReturn[1] = "";
			tempReturn[0] = "";
		}

		return tempReturn;
	}
}