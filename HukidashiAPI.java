package anaso.HukidashiChat;

import org.bouncycastle.util.test.Test;

public class HukidashiAPI
{
	static HukidashiChatTick hukidashiChatTick;

	public HukidashiAPI(HukidashiChatTick hukidashiChatTick)
	{
		this.hukidashiChatTick = hukidashiChatTick;
	}

	static public void setHukidashiSendMe(String nameString, String chatString)
	{
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, true);
	}

	static public void setHukidashi(String nameString, String chatString, boolean sendMeFlag)
	{
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, false);
	}

	static public void setHukidashi(String nameString, String chatString, double posX, double posY, double posZ)
	{
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.setBufferedHukidashiValues(sendStrings, posX, posY, posZ);
	}

	static public void setHukidashi(String nameString, String[] chatStrings, double posX, double posY, double posZ)
	{
		hukidashiChatTick.setBufferedHukidashiValues(nameString, chatStrings, posX, posY, posZ);
	}
}