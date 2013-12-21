package anaso.HukidashiChat;

class HukidashiValues
{
	int listNumber = 0;  // このインスタンスの番号(超重要?)

	String nameString;  // 名前欄
	String sendPlayerString;  // 送信者の名前
	String chatString;  // 表示するメッセージ

	String[] trimmedChatStrings;  // 区切った後のメッセージ

	// GUIの位置
	int guiPositionX = 0;
	int guiPositionY = 0;

	// 表示時間
	int fadeinTime = 0;
	int viewTime = 0;
	int fadeoutTime = 0;

	// 自分が送信したことにするフラグ
	boolean sendMeFlag = false;

	int guiNearPointX = 0;
	int guiNearPointY = 0;

	// 特定の位置を指定する場合
	boolean isPlayer = true;

	// ワールドでの位置 X,Y,Z
	double[] worldPosition;

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
		if(this.nameString == null)
		{
			this.nameString = nameString;
		}

		if (this.sendPlayerString == null)
		{
			this.sendPlayerString = nameString;
		}

		if(this.chatString == null)
		{
			this.chatString = chatString;
		}
	}

	void setTrimmedChatStrings(String[] trimmedStrings)
	{
		if(this.trimmedChatStrings == null)
		{
			this.trimmedChatStrings = trimmedStrings;
		}
	}

	void setSendPlayerString(String sendPlayerString)
	{
		this.sendPlayerString = sendPlayerString;
	}

	void setGuiNearPoint(int[] guiNearPoint)
	{
		this.guiNearPointX = guiNearPoint[0];
		this.guiNearPointY = guiNearPoint[1];
	}

	void setWorldPosition(double[] worldPosition)
	{
		this.worldPosition = worldPosition;
		this.isPlayer = false;
	}
}
