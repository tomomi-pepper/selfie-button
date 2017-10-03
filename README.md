# CameraButton

<img src="https://linkingiot.com/files/images/devices/devicesImg01.png" /><br/>
[Project Linking](https://linkingiot.com/)対応デバイスの[Pochiru](https://ssl.braveridge.com/store/html/products/detail.php?product_id=30)
を使って、リモートでカメラのシャッターを切ることができるアプリです。

# 準備

まず、CameraButtonのアプリをインストールします。
[Linkingアプリ](https://play.google.com/store/apps/details?id=com.nttdocomo.android.smartdeviceagent)で[Pochiru](https://ssl.braveridge.com/store/html/products/detail.php?product_id=30)と連携しておきます。<br><br>
<img src="./linking.png" width="150px"><br/>

[Linkingアプリ](https://play.google.com/store/apps/details?id=com.nttdocomo.android.smartdeviceagent)のアプリ連携でCameraButtonをONにします。<br><br>
<img src="./app.png" width="150px"><br/>


開発者モードをON、デバッグをONにして、端末とUSB接続し、<br/>
<code>adb tcpip 5555</code><br/>
とコマンドを打って、adbをtcpipモードにしておく必要があります。


アプリを起動後に「開始」ボタンを押すと、<br/>
<img src="./dialog.png" width="150px"><br/>
の画面が出るので、「OK」を押します。

そしてカメラアプリを起動した状態でPochiruを押してください。

※<code>KEYCODE_CAMERA</code>を<br/>
<code>/system/bin/input keyevent KEYCODE_CAMERA</code><br/>
で投げていますが、カメラアプリによっては反応しない場合があるかもしれません。

※他のアプリがフォアグラウンドの時にキーイベントを投げるため、[adb接続ライブラリ](https://github.com/cgutman/AdbLib)を使って、自分自身にadb接続しています。

