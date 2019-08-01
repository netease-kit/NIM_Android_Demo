# 网易云通信安卓 Demo 结构说明

网易云通信 Demo 工程基于网易云通信 SDK，演示了 SDK 聊天、群组、白板、实时音视频等功能接口的使用方法。Demo 工程依赖于 UIKit 工程，UIKit 实现了基本的消息收发，群组服务以及通讯录等功能，包含有完整的界面显示。开发者可以直接调用UIKit 中的接口，来进行功能开发，加快开发速度。用户可参照该 Demo，将网易云通信 SDK 接入自己的 APP。

## <span id="工程导入指引"> 工程导入指引</span>
- [Eclipse导入Demo](http://note.youdao.com/groupshare/?token=7565E66468734B5C89D114AFD7AAB493&gid=14302436  "target=_blank")
- [Eclipse导入UIKit](http://note.youdao.com/groupshare/?token=8F876090BE4E4D55B88A9ED04E8513F2&gid=14302436  "target=_blank")
- [Android Studio导入Demo](http://note.youdao.com/groupshare/?token=94E0368010384F5FB2D6E1E2C5855AA8&gid=14302436  "target=_blank")
- [Android Studio导入UIKit](http://note.youdao.com/groupshare/?token=9C35BEC0231C4E80B1DBF76FBDC54161&gid=14302436  "target=_blank")
- [UIKit集成示例](http://note.youdao.com/groupshare/?token=F0EF37ECED6541E58634EF0AFBB451CF&gid=14302436  "target=_blank")
- [Android视频教程源码及说明](http://note.youdao.com/groupshare/?token=72C14A95D15144259B5E5C01D583D639&gid=14302436  "target=_blank")

## <span id="下载编译 Demo"> 下载编译 Demo</span>

用户可在下载页面进行下载 Demo 源码工程。

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
- 将 UIKit 下 AndoridManifeset 文件下所有 Activity 声明复制到 APP 工程的 AndoridManifeset 文件中。
- 将 UIKit 下 assets 目录下所有资源复制到 APP工程的 assets 目录下。
>由于 google 已经将 Android Studio 作为官方开发工具，不再提供 ADT Bundle 下载，继续使用 Eclipse 开发会让你错过很多新特性，因此我们强烈建议你使用 Android Studio 开发基于网易云通信 SDK 的 APP，该 Demo的很多自动化特性也将只有 Android Studio 才能体验到。

## <span id="源码结构"> 源码结构</span>

由于 Demo 依赖于 UIKit 进行开发。分为 Demo 工程和 UIKit 工程。分别介绍这两个工程的源码结构。

- Demo日志地址：**/{外卡根目录}/{app\_package\_name}/log/**

### <span id="Demo源码结构"> Demo源码结构</span>

- Application 入口：NimApplication, 包含 SDK 的初始化，UIKit的初始化以及配置示例。
- 登录相关：login 包，包含一个比较典型的从第三方 APP 授权，然后登录到网易云通信服务器的例子。
- 主界面：main 包，包含最近联系人列表和好友/群组列表。该包作为各个功能的入口点，内含获取和管理最近联系人，获取群组列表，收发自定义通知等 SDK 接口使用示例。
- 消息相关：session 包，包含消息历史，聊天信息和搜索消息界面。消息的具体展示和收发都在 UIKit 中。可以使用 NimUIKit 直接展示和使用。也可以使用 SessionHelper 进行消息界面的定制。
- 群组相关：team 包，包含搜索群组和加入群组界面。包含创建高级群和讨论组的接口使用示例。
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

网易云通信 Demo 实现了一个 IM 软件的所有基础功能，开发者可直接以 Demo 为基础开发自己的 IM 软件，也可以稍作修改，用于前期流程验证，也可以作为 SDK 开发的参考和指南。

- 如果你已经在网易云通信官网上注册了 APP，你需要修改 AndroidManifest 中的 “com.netease.nim.appKey” 为你自己的 appkey，否则登录会失败。

## <span id="聊天界面代码说明"> 聊天界面代码说明</span>

### 结构说明

- BaseMessageActivity：聊天界面基类。封装了 MessageFragment 和 actionbar 相关操作。
- MessageFragment：聊天界面基类 Fragment。
- MessageListPanel：消息收发模块。包括收发消息的显示，消息列表相关操作。
- InputPanel：底部文本编辑，语音等模块。包括文本，语音的消息发送和更多中的 action 操作。
- ActionsPanel：更多操作模块。
- P2PMessageActivity：点对点聊天界面。
- TeamMessageActivity： 群聊界面。
- SessionCustomization：聊天界面定制化参数。
- MessageLoader：MessageListPanel 中的内部类，用于加载消息。
- MsgItemEventListener： MessageListPanel 中的内部类，用于重发消息，长按消息相关操作等。

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

```java
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

```java
avChatUI.outGoingCalling(receiverId, AVChatType.typeOfValue(state));
```

2、通知界面刷新，详见[界面刷新](#界面刷新) 一节。

```java
if (callTypeEnum == AVChatType.AUDIO) {
	onCallStateChange(CallStateEnum.OUTGOING_AUDIO_CALLING);
} else {
	onCallStateChange(CallStateEnum.OUTGOING_VIDEO_CALLING);
}
```

3、发起通话

```java
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

```java
avChatUI.inComingCalling(avChatData);
```

2、通知界面刷新，详见[界面刷新](#界面刷新) 一节。

```java
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

```java
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

```java
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

## <span id="聊天室代码说明"> 聊天室代码说明</span>

### 结构说明

- activity包：界面相关
- adapter包：适配器相关
- constant包：常量定义
- fragment包：界面相关。其中包括 tab 包，定义了聊天室框架中的 tab fragment。
- helper包：缓存管理，通知类消息字段管理等辅助功能。
- module包：模块化相关。包括聊天室收发消息模块。
- thirdparty包：第三方实现相关。包括网易云通信 demo 聊天室 http 客户端。
- viewholder包：界面相关 viewholder 展示。
- widget包：聊天室相关控件

### 重点类说明

- ChatRoomsFragment：直播间列表 fragment。包含向网易云通信Demo应用服务器请求聊天室列表操作。
- ChatRoomActivity：聊天室界面。封装了 ChatRoomFragment。包括聊天室的进入/离开的操作，监听聊天室在线状态和监听聊天室被踢出状态。
- ChatRoomTabFragment：聊天室内 tab fragment 的基类。
- ChatRoomFragment：聊天室 Activity 包含的顶层 Fragment。包括界面上方界面和下方的 viewpager。viewpager 包含3个 tab fragment，分别是：MessageTabFragment （直播互动 tab）， MasterTabFragment （主播 tab）， OnlinePeopleTabFragment （在线成员 tab）。
- MessageTabFragment：直播互动基类 fragment。内嵌 ChatRoomMessageFragment （直播互动 fragment）。
- MasterTabFragment：主播基类 fragment。内嵌 MasterFragment（主播 fragment ）。
- OnlinePeopleTabFragment：在线成员基类 fragment。内嵌 OnlinePeopleFragment （在线成员 fragment）
- ChatRoomMessageFragment：聊天室直播互动 fragment。包括消息的收发相关操作。
- MasterFragment： 聊天室主播 fragment。包括获取聊天室资料等操作。
- OnlinePeopleFragment：聊天室在线人数 fragment。包括获取聊天室成员信息等操作。
- ChatRoomMsgViewHolderFactory:  聊天室消息项展示ViewHolder工厂类。包括消息展示 viewholder 的注册操作。

## <span id="新老版本兼容问题"> 新老版本兼容问题</span>

### 群通知相关

问题：群通知新增的通知消息类型，可能会造成老版本崩溃。
原因：TeamNotificationHelper#buildUpdateTeamNotification 的 a.getUpdatedFields() 的 size 为0，造成 sb 的 length为0，会抛出 StringIndexOutOfBoundsException 错误。
解决方案：判断 sb 的length，参考demo。

## <span id=" Android 6.0 权限管理"> Android 6.0 权限管理 </span>

网易云通信 demo 提供 Android 6.0 权限管理示例。相关方法的实现，在 uikit 的 permission 包中。

在需要相关权限的地方，发起申请并等待用户操作后的返回结果。具体实现方法：

```java
private void requestBasicPermission() {
	MPermission.with(MainActivity.this)             
		.addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
		.permissions(
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			// ……
		)
		.request();
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
}

@OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
public void onBasicPermissionSuccess(){
	Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
}

@OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
public void onBasicPermissionFailed(){
	Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
}
```
