# 网易云信安卓 Demo 结构说明

网易云信 Demo 工程基于网易云信 SDK，演示了 SDK 聊天、群组、白板、实时音视频等功能接口的使用方法。Demo 工程依赖于 UIKit 工程，UIKit 实现了基本的消息收发，群组服务以及通讯录等功能，包含有完整的界面显示。开发者可以直接调用UIKit 中的接口，来进行功能开发，加快开发速度。用户可参照该 Demo，将网易云信 SDK 接入自己的 APP。

## <span id="下载编译 Demo"> 下载编译 Demo</span>

用户可在[网易云信官网](http://netease.im/?page=download  "target=_blank")下载 Demo 源码工程。

总体环境需求：

- 我们的部分代码会针对不同系统版本做优化，在新的平台上使用新的 API。编译运行该 Demo 的 build tools最低版本要求为19。
- 由于 Demo 中使用到了部分 java7 以上的特性，因此 JDK 请使用 JDK7 或以上版本。

如果你使用的 IDE 是 Android Studio，可直接在 IDE 中打开 Demo 工程，然后将工程目录下 gradle.properties 文件按照注释修改，就可以直接编译运行。

- 如果你是第一次使用 Android Studio，导入时会从 gradle 网站下载 gradle 发布包，在国内下载可能会比较慢。这时你也可以通过 Android Studio 设置，不使用 gradle wrapper，改为使用 local gradle distribution。

如果你使用的 IDE 是 Eclipse，可直接在 IDE 中打开工程，做如下修改后，即可编译运行。

- 由于在 Eclipse 配置 libs 外的 jar 包比较麻烦，且不能配置 jni 库的位置，因此你需要把 libs-sdk 的所有文件夹移动到 libs 目录下（Demo 1.5版本及以后不需要）。
- 注释掉 AboutActivity 中 initViewData() 函数的函数体。这行使用 Android Studio 编译期添加 BuildConfig 字段的特性，在 Eclipse 上无法完成，直接注释掉即可。
- 如果你需要试验发送地理位置功能，请将 AndroidManifest 中的 {AMAP\_KEY} 替换为 demo/build.gradle 文件中 buildTypes 内对应的值。在 Android Studio 上会在编译器自动替换。
- 由于需要使用 JDK7 编译，旧版本的 ADT 插件仅支持到1.6，因此你需要使用最新的 ADT 版本（23.0及以上）才能正确编译 Demo。

>由于 google 已经将 Android Studio 作为官方开发工具，不再提供 ADT Bundle 下载，继续使用 Eclipse 开发会让你错过很多新特性，因此我们强烈建议你使用 Android Studio 开发基于网易云信 SDK 的 APP，该 Demo的很多自动化特性也将只有 Android Studio 才能体验到。

## <span id="导入 Demo 工程示例"> 导入 Demo 工程示例</span>
- [Eclipse导入Demo](http://note.youdao.com/share/?id=0bb0b773bb427938d96928ec31bf2b2d&type=note  "target=_blank")
- [Android Studio导入Demo](http://note.youdao.com/share/?id=fb2ddcd1f5b15cc725a4a6df05f6317a&type=note  "target=_blank")
- [Eclipse导入UIKit](http://note.youdao.com/share/?id=a8e904df99e1a114c5b565568a19906d&type=note  "target=_blank")
- [Android Studio导入UIKit](http://note.youdao.com/share/?id=66d12a2aa10b37928b869feaef54ec3e&type=note  "target=_blank")

## <span id="源码结构"> 源码结构</span>

由于 Demo 依赖于 UIKit 进行开发。分为 Demo 工程和 UIKit 工程。分别介绍这两个工程的源码结构。

### <span id="Demo源码结构"> Demo源码结构</span>

- Application 入口：NimApplication, 包含 SDK 的初始化，UIKit的初始化以及配置示例。
- 登录相关：login 包，包含一个比较典型的从第三方 APP 授权，然后登录到网易云信服务器的例子。
- 主界面：main 包，包含最近联系人列表和好友/群组列表。该包作为各个功能的入口点，内含获取和管理最近联系人，获取群组列表，收发自定义通知等 SDK 接口使用示例。
- 消息相关：session 包，包含消息历史，聊天信息和搜索消息界面。消息的具体展示和收发都在 UIKit 中。可以使用 NimUIKit 直接展示和使用。也可以使用 SessionHelper 进行消息界面的定制。
- 群组相关：team 包，包含搜索群组和加入群组界面。包含创建高级群和普通群的接口使用示例。
- 音视频通话相关：avchat 包，包含音视频通话界面。内含网络通话功能的 SDK 接口使用示例，以及一个完整的网络通话流程示例，开发者可直接参考开发音视频通话功能。
- 实时会话（白版）：rts 包，包含白板教学的示例，支持实时音频、白板数据收发。

### <span id="UIKit源码结构"> UIKit源码结构</span>

- UIKit 调用接口：NimUIKit，包含构建好友和群的缓存，打开聊天窗口，打开联系人选择器，打开群资料。
- 基础组件相关：common 包， 包含 Activity、Fragment 和 Adapter的基类，一些自定义 UI 控件以及系统工具类。
- 消息相关：session包，包含单聊/群聊界面，内含收发消息，上传下载消息附件，使用高清语音，管理消息历史等 SDK 接口使用示例。该包还有消息展示，使用 emoji 表情，发送图片等示例代码，可供开发者参考。
- 群组相关：team 包，包含群组信息界面。内含展示群资料，修改群资料，展示群成员列表，管理群成员列表等 SDK 接口使用示例。
- 最近联系人相关：recent 包，包含最近联系人列表界面
- 通讯录相关：contact_selector 包和 contact 包，包含联系人选择器和通讯录。内含用户信息等 SDK 接口使用示例。

## <span id="修改Demo为己用"> 修改Demo为己用</span>

网易云信 Demo 实现了一个 IM 软件的所有基础功能，开发者可直接以 Demo 为基础开发自己的 IM 软件，也可以稍作修改，用于前期流程验证，也可以作为 SDK 开发的参考和指南。

- 如果你已经在网易云信官网上注册了 APP，你需要修改 AndroidManifest 中的 “com.netease.nim.appKey” 为你自己的 appkey，否则登录会失败。

## <span id="音视频代码说明"> 音视频代码说明</span>

### 结构说明

- AVChatActivity:：音视频界面
- AVChatUI：音视频管理类, 音视频相关功能管理，内含音频管理，视频管理，视频绘制管理。还包括了网络连接状态等监听事件的处理，具体见开发手册。
- AVChatAudio：音频管理， 音频界面初始化和界面相关操作管理
- AVChatVideo：视频管理， 视频界面初始化和界面相关操作管理
- AVChatSurface：视频绘制管理
- AVChatNotification：音视频聊天通知栏
- AVChatUIListener：音视频界面操作监听

### <span id="初始化"> 初始化</span>

在 `AVChatActivity` 的 oncreate 中，进行管理器的初始化工作

```
avChatUI = new AVChatUI(this, root, this);
if (!avChatUI.initiation()) {
	this.finish();
	return;
}
public boolean initiation() {
        AVChatProfile.getInstance().setAVChatting(true);
        avChatAudio = new AVChatAudio(root.findViewById(R.id.avchat_audio_layout), this, this);
        avChatVideo = new AVChatVideo(context, root.findViewById(R.id.avchat_video_layout), this, this);
        avChatSurface = new AVChatSurface(context, this, root.findViewById(R.id.avchat_surface_layout));

        return true;
    }
```

### <span id="拨打"> 拨打</span>

主流程：

1、传入参数，对方帐号和拨打的类型（AVChatType.AUDIO 或 AVChatType.VIDEO）。

```
avChatUI.outGoingCalling(receiverId, AVChatType.typeOfValue(state));
```

2、通知界面刷新，详见[界面刷新](#界面刷新) 一节。

```
if (callTypeEnum == AVChatType.AUDIO) {
	onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
} else {
	onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
}
```

3、发起通话

```
/**
* 发起通话
* account 对方帐号
* callTypeEnum 通话类型：语音、视频
* videoParam 发起视频通话时传入，发起音频通话传null
* AVChatCallback 回调函数，返回AVChatInfo
*/
AVChatManager.getInstance().call(account, callTypeEnum, videoParam, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
               ...
            }

            @Override
            public void onFailed(int code) {
                ...
            }

            @Override
            public void onException(Throwable exception) {
                ...
            }
        });
```

### <span id="接听"> 接听</span>

1、传入参数 AVChatData

```
avChatUI.inComingCalling(avChatData);
```

2、通知界面刷新，详见[界面刷新](#界面刷新) 一节。

```
if (callTypeEnum == AVChatType.AUDIO) {
	onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
} else {
	onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
}
```

### <span id="界面刷新"> 界面刷新</span>

界面刷新，详细流程如下。
1、调用 onCallStateChange
2、如果界面没有进行过初始化，则进行界面初始化 findViews，并为各个按钮添加响应事件。
3、根据 CallStateEnum 判断界面布局设置和显隐性。

```
// 有来电，界面状态更新
onCallStateChange(CallStateEnum.INCOMING_AUDIO_CALLING);

// 判断来电类型，是音频或视频
if(CallStateEnum.isAudioMode(state))
	findViews();

// 设置信息显示和界面布局
switch (state){
	...
	case INCOMING_AUDIO_CALLING://免费通话请求
		setSwitchVideo(false);
		showProfile();//对方的详细信息
		showNotify(R.string.avchat_audio_call_request);
		setMuteSpeakerHangupControl(false);
		setRefuseReceive(true);
		receiveTV.setText(R.string.avchat_pickup);
		break;
	...
}
```

### <span id="按钮响应事件"> 按钮响应事件</span>

AVChatAudio 和 AVChatVideo 中包含了挂断，拒绝，接受，禁音，开启扬声器，音视频切换和摄像头切换的操作。 按钮的点击响应事件，通过 AVChatUIListener 统一交给 AVChatUI 进行管理。示例如下：

```
// 初始化挂断按钮
hangup = mute_speaker_hangup.findViewById(R.id.avchat_audio_hangup);
hangup.setOnClickListener(this);

// 按钮响应事件处理
public void onClick(View v) {
	switch (v.getId()) {
		case R.id.avchat_audio_hangup:
			listener.onHangUp();
		break;
	...
	}
}

// 在AVChatUI的AVChatUIListener实现中，实现挂断或取消接口。
public void onHangUp() {
	if (isCallEstablish.get()) {
		hangUp(AVChatExitCode.HANGUP);
	} else {
		hangUp(AVChatExitCode.CANCEL);
	}
}
```