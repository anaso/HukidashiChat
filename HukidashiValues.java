package anaso.HukidashiChat;

class HukidashiValues
{
	int listNumber = 0;  // このインスタンスの番号(超重要?)

	String nameString = "";  // 名前欄
	String chatString = "";  // 表示するメッセージ

	// GUIの位置
	int guiPositionX = 0;
	int guiPositionY = 0;

	// 表示時間
	int fadeinTime = 0;
	int viewTime = 0;
	int fadeoutTime = 0;

	// 送信者の名前
	String sendPlayerString = "";
	// 自分が送信したことにするフラグ
	boolean sendMeFlag = false;


	// コンストラクタ
	HukidashiValues(int listNumber)
	{
		this.listNumber = listNumber;
	}

	// 表示時間
	void setViewTimes(int fadeinTime, int viewTime, int fadeoutTime)
	{
		this.fadeinTime = fadeinTime;
		this.viewTime = viewTime;
		this.fadeoutTime = fadeoutTime;
	}

	// GUIの位置
	void setGuiViewPosition(int posX, int posY)
	{
		this.guiPositionX = posX;
		this.guiPositionY = posY;
	}

	// 名前とチャット内容
	void setHukidashiStrings(String nameString, String chatString)
	{
		this.nameString = nameString;
		this.sendPlayerString = nameString;

		this.chatString = chatString;
	}

	void setSendPlayerString(String sendPlayerString)
	{
		this.sendPlayerString = sendPlayerString;
	}
}
