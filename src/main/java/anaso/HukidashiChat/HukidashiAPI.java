package anaso.HukidashiChat;

public class HukidashiAPI{
	static HukidashiChatTick hukidashiChatTick;

	public HukidashiAPI(HukidashiChatTick hukidashiChatTick){
		this.hukidashiChatTick = hukidashiChatTick;
	}

	static public void setHukidashiSendMe(String nameString, String chatString){
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, true);
	}

	static public void setHukidashi(String nameString, String chatString, boolean sendMeFlag){
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, sendMeFlag);
	}

	static public void setHukidashi(String nameString, String chatString, double posX, double posY, double posZ){
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, posX, posY, posZ);
	}

	static public void setHukidashi(String nameString, String[] chatStrings, double posX, double posY, double posZ){
		hukidashiChatTick.setBufferedHukidashiValues(nameString, chatStrings, posX, posY, posZ);
	}
}