HukidashiChat

Minecraft Mod

チャットが吹き出しで表示されるようになります。

・使い方
設定ファイルはHukidashiChat.cfgです。

描画位置の設定にクセがあり、MinecraftのGUIスケールがNormalのときは値が2倍、
Largeのときは値が3倍されます。（多分

設定のカテゴリー分けを行いました。

-設定内容-
 -基本-
Display time -> 画面に表示している時間です。 値は1/20秒です。
Fade-in Time , Fade-out Time -> フェードインとフェードアウトの時間です。 DisplayTimeとは別にカウントします。
Gui Alpha -> 吹き出しの透明度です。値は0-255の範囲で、0で見えなくなります。
Mute Message -> 吹き出しにしない言葉を設定します。改行して区切ってください。また、デフォルトのチャット蘭には表示されます。
Mute Player -> 表示しないプレイヤーを設定します。区切りは改行で
Include My Message -> 自分の発言を表示するかの設定です。 true=表示する
View All Message -> 距離やディメンジョンにかかわらず表示します。true=表示する
View Hukidashi -> 吹き出しを表示するかしないかです。処理がごちゃごちゃなので重いと思ったらfalseでオフにしてください。 true=表示する
Player Space -> View All Messageがオフのときや、吹き出しを表示するまでの、自分と相手との距離です。

 -吹き出しのテクスチャを変更した際に設定する内容-
Gui Texture Size -> 表示するテクスチャ（hukidashi_gui.png）のサイズを入力してください。
                    また、テクスチャは256x256のサイズにして、表示しない部分は透過させてください。
Player Name Pos -> プレイヤー名の位置です。画像の右上が0です。
Chat Text Pos -> チャット本文の位置です。画像の右上が0です。
Hukidashi Rotation -> 吹き出し（hukidashi.png）の回転軸の位置
Hukidashi Texture Size -> 吹き出し（hukidashi.png）のサイズ。注意事項は（hukidashi_gui.png）と同じです。
Hukidashi In Gui -> 吹き出し（hukidashi.png）がメインGUI（hukidashi_gui.png）にめり込むサイズです、空白ができちゃうときに

 - GUIの位置設定 gui_position -
Gui Pos -> 吹き出しの位置です。左上を0と取ります。また、マイナスの値は右下を0と取ります。

 -文字の色 text_color-
Player Name Color -> プレイヤーの名前の色の設定です。値は0-255まで
Player Name Shadow -> 文字に影をつけるか。 true=つける false=つけない
Chat Text Color -> チャット本文の色の設定です。値は0-255まで
Chat Text Shadow -> 文字に影をつけるか。 true=つける false=つけない

・インストール
MinecraftForgeを導入することで作成されたmodsフォルダに、zipファイルを入れてください。
Mod作成に使用したMinecraftForgeのバージョンはファイル名を参照してください。

・外部連携
他のModと連携ができるようになりました。
anaso.HukidashiChat.HukidashiAPI.setHukidashi(hoge hoge)のクラスメソッドに命令を投げ込みます。
私のModでも連携しているので、参考にしてください。 https://github.com/anaso/ReadSign/blob/master/ReadSignKey.java
現在使用できるクラスメソッドを以下に記します。

setHukidashi(String nameString, String chatString)
一番シンプルなAPI、自分が発言したことにするので、どこにいても表示されます。
nameStringに発言した人や物、chatStringにチャットとして表示する内容を渡してください。

setHukidashi(String nameString, String chatString, boolean sendMeFlag)
上の拡張型です。sendMeFlagをfalseにすることで、nameStringのプレイヤーが発言したことにします。相手の名前は正しく入力しましょう。

static public void setHukidashi(String nameString, String chatString, double posX, double posY, double posZ)
自分やプレイヤーではなく、特定の座標で発言したい場合に使用します。double型なので、小数点でもOK

static public void setHukidashi(String nameString, String[] chatStrings, double posX, double posY, double posZ)
上の拡張型です。チャットの引数が配列になっており、4つまでの配列ならば改行して表示します。