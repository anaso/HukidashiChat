package anaso.HukidashiChat;

import org.bouncycastle.util.test.Test;

public class HukidashiAPI
{
	static HukidashiChatTick hukidashiChatTick;

	public HukidashiAPI(HukidashiChatTick hukidashiChatTick)
	{
		this.hukidashiChatTick = hukidashiChatTick;
	}

	static public void setHukidashi(String nameString, String chatString)
	{
		String[] sendStrings = {nameString, chatString};
		hukidashiChatTick.bufferedHukidashi(sendStrings);
	}

	static public void setHukidashi(String nameString, String cString, int blockX, int blockY, int blockZ)
	{

	}
}