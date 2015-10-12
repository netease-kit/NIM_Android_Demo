# 网易云信安卓 demo 结构说明

网易云信 demo 工程基于网易云信 SDK，演示了 SDK 聊天，群组，实时音视频等功能接口的使用方法。用户可参照该 demo，将网易云信 SDK 接入自己的 APP。

## 下载编译 Demo

用户可在[网易云信官网](http://netease.im/base.html?page=download  "target=_blank")下载demo源码工程。

总体环境需求：

- 我们的部分代码会针对不同系统版本做优化，在新的平台上使用新的 API。编译运行该 demo 的 build tools最低版本要求为18。
- 由于 demo 中使用到了部分 java7 以上的特性，因此 JDK 请使用 JDK7 或以上版本。

如果你使用的 IDE 是 Android Studio，可直接在 IDE 中打开 demo 工程，然后将工程目录下 gradle.properties文件按照注释修改，就可以直接编译运行。

- 如果你是第一次使用 Android Studio，导入时会从 gradle 网站下载 gradle 发布包，在国内下载可能会比较慢。这时你也可以通过 Android Studio 设置，不使用 gradle wrapper，改为使用 local gradle distribution。

如果你使用的 IDE 是 Eclipse，可直接在 IDE 中打开工程，做如下修改后，即可编译运行。

- 由于在 Eclipse 配置 libs 外的 jar 包比较麻烦，且不能配置 jni 库的位置，因此你需要把 libs-sdk 的所有文件夹移动到 libs 目录下。
- 注释掉 AboutActivity 中 initViewData() 函数的函数体。这行使用 Android Studio 编译期添加 BuildConfig 字段的特性，在 Eclipse 上无法完成，直接注释掉即可。
- 如果你需要试验发送地理位置功能，请将 AndroidManifest 中的 {AMAP\_KEY} 替换为 demo/build.gradle 文件中 buildTypes 内对应的值。在 Android Studio 上会在编译器自动替换。
- 由于需要使用 JDK7 编译，旧版本的 ADT 插件仅支持到1.6，因此你需要使用最新的 ADT 版本（23.0及以上）才能正确编译 demo。

>由于 google 已经将 Android Studio 作为官方开发工具，不再提供 ADT Bundle 下载，继续使用 Eclipse 开发会让你错过很多新特性，因此我们强烈建议你使用 Android Studio 开发基于网易云信 SDK 的 APP，该 demo的很多自动化特性也将只有 Android Studio 才能体验到。

## 源码结构

- Application 入口：NimApplication, 包含 SDK 的初始化以及配置示例。
- 登录相关：login 包，包含一个比较典型的从第三方 APP 授权，然后登录到网易云信服务器的例子。
- 主界面：main 包，包含最近联系人列表和好友/群组列表。该包作为各个功能的入口点，内含获取和管理最近联系人，获取群组列表，收发自定义通知等 SDK 接口使用示例。
- 消息相关：session 包，包含单聊/群聊界面。内含收发消息，上传下载消息附件，使用高清语音，管理消息历史等 SDK 接口使用示例。该包还有消息展示，使用 emoji 表情，发送图片等示例代码，可供开发者参考。
- 群组相关：team 包，包含群组信息界面。内含展示群资料，修改群资料，展示群成员列表，管理群成员列表等 SDK 接口使用示例。
- 网络通话相关：avchat 包，包含网络通话界面。内含网络通话功能的 SDK 接口使用示例，以及一个完整的网络通话流程示例，开发者可直接参考开发网络通话功能。


## 修改demo为己用

网易云信 demo 实现了一个 IM 软件的所有基础功能，开发者可直接以 demo 为基础开发自己的 IM 软件，也可以稍作修改，用于前期流程验证，SDK 开发指导等功能。

- 如果你已经在网易云信官网上注册了 APP，你需要修改 AndroidManifest 中的 “com.netease.nim.appKey” 为你自己的 appkey，否则登录会失败。
- 你需要修改 demo 的好友获取方式。网易云信 demo 获取好友的代码在 .contact.protocol 包下，而用户资料缓存你可以直接重用。
