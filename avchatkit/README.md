# AVChatKit 使用说明

## <span id="全局配置项 AVChatOptions">全局配置项 AVChatOptions</span>

AVChatKit 组件提供了全局配置类 AVChatOptions，初始化 AVChatKit 时传入 AVChatOptions 对象。

|类型|AVChatOptions 属性|说明|
|:---|:---|:---|
|Class<? extends Activity>|entranceActivity|通知入口|
|int|notificationIconRes|通知栏icon|
|void|logout(Context context)|被踢出时，调用的方法|

## <span id="初始化">初始化</span>

在应用的 Application 的 **主进程** 中初始化 AVChatKit。

```java
AVChatOptions avChatOptions = new AVChatOptions(){
    @Override
    public void logout(Context context) {
        // 主程序登出操作
    }
};
// 点击通知栏，入口Activity
avChatOptions.entranceActivity = WelcomeActivity.class;
// 通知栏图标icon
avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
// 初始化 AVChatKit
AVChatKit.init(avChatOptions);
```

- 示例

```java
AVChatOptions avChatOptions = new AVChatOptions(){
    @Override
    public void logout(Context context) {
        MainActivity.logout(context, true);
    }
};
avChatOptions.entranceActivity = WelcomeActivity.class;
avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
AVChatKit.init(avChatOptions);

// 初始化日志系统
LogHelper.init();
// 设置用户相关资料提供者
AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
    @Override
    public UserInfo getUserInfo(String account) {
        return NimUIKit.getUserInfoProvider().getUserInfo(account);
    }

    @Override
    public String getUserDisplayName(String account) {
        return UserInfoHelper.getUserDisplayName(account);
    }
});
// 设置群组数据提供者
AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
    @Override
    public String getDisplayNameWithoutMe(String teamId, String account) {
        return TeamHelper.getDisplayNameWithoutMe(teamId, account);
    }

    @Override
    public String getTeamMemberDisplayName(String teamId, String account) {
        return TeamHelper.getTeamMemberDisplayName(teamId, account);
    }
});
```

AVChatKit 中用到的 Activity 已经在 AVChatKit 工程的 AndroidManifest.xml 文件中注册好，上层 APP 无需再去添加注册。

## <span id="快速使用">快速使用</span>

### <span id="发起点对点音视频通话呼叫">发起点对点音视频通话呼叫</span>

- API 原型

```java
/**
 * 发起音视频通话呼叫
 * @param context   上下文
 * @param account   被叫方账号
 * @param displayName   被叫方显示名称
 * @param callType      音视频呼叫类型
 * @param source        发起呼叫的来源，参考AVChatActivityEx.FROM_INTERNAL/FROM_BROADCASTRECEIVER
 */
public static void outgoingCall(Context context, String account, String displayName, int callType, int source);
```

- 参数介绍

|参数|说明|
|:---|:---|
|context   |上下文|
|account   |被叫方账号|
|displayName   |被叫方显示名称|
|callType      |音视频呼叫类型|
|source        |发起呼叫的来源，参考AVChatActivityEx.FROM_INTERNAL/FROM_BROADCASTRECEIVER|

- 示例

```java
AVChatKit.outgoingCall(context, "testAccount", "displayName" AVChatType.AUDIO, AVChatActivity.FROM_INTERNAL);
```

### <span id="发起群组音视频通话呼叫">发起群组音视频通话呼叫</span>

-  API 原型

```java
/**
 * 发起群组音视频通话呼叫
 * @param context   上下文
 * @param receivedCall  是否是接收到的来电
 * @param teamId    team id
 * @param roomId    音视频通话room id
 * @param accounts  音视频通话账号集合
 * @param teamName  群组名称
 */
public static void outgoingTeamCall(Context context, boolean receivedCall, String teamId, String roomId, ArrayList<String> accounts, String teamName);
```

- 参数说明

|参数|说明|
|:---|:---|
|context   |上下文|
|receivedCall  |是否是接收到的来电|
|teamId    |team id|
|roomId    |音视频通话 room id|
|accounts  |音视频通话账号集合|
|teamName  |群组名称|

- 示例

```java
// 以下参数为示例
AVChatKit.outgoingTeamCall(context, false, "1111", "roomName", accounts, "teamName");
```

### <span id="打开网络通话设置界面">打开网络通话设置界面</span>

- API 原型

```java
/**
 * 打开网络通话设置界面
 * @param context   上下文
 */
public static void startAVChatSettings(Context context);
```

- 示例

```java
AVChatKit.startAVChatSettings(SettingsActivity.this);
```