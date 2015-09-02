# 网易云信安卓demo结构说明

网易云信demo工程基于网易云信SDK，演示了SDK聊天，群组，实时音视频等功能接口的使用方法。用户可参照该demo，将网易云信SDK接入自己的app。

## 下载编译Demo

用户可在[网易云信官网](http://netease.im/base.html?page=download  "target=_blank")下载demo源码工程。

总体环境需求：

- 我们的部分代码会针对不同系统版本做优化，在新的平台上使用新的API。编译运行该demo的build tools最低版本要求为18。
- 由于demo中使用到了部分java7以上的特性，因此JDK请使用JDK7或以上版本。

如果你使用的IDE是Android Studio，可直接在IDE中打开demo工程，然后将工程目录下gradle.properties文件按照注释修改，就可以直接编译运行。

- 如果你是第一次使用Android Studio，导入时会从gradle网站下载gradle发布包，在国内下载可能会比较慢。这时你也可以通过Android Studio设置，不使用gradle wrapper，改为使用local gradle distribution。

如果你使用的IDE是Eclipse，可直接在IDE中打开工程，做如下修改后，即可编译运行。

- 由于在Eclipse配置libs外的jar包比较麻烦，且不能配置jni库的位置，因此你需要把libs-sdk的所有文件夹移动到libs目录下。
- 注释掉AboutActivity中initViewData()函数的函数体。这行使用Android Studio编译期添加BuildConfig字段的特性，在Eclipse上无法完成，直接注释掉即可。
- 如果你需要试验发送地理位置功能，请将AndroidManifest中的{AMAP\_KEY}替换为demo/build.gradle文件中buildTypes内对应的值。在Android Studio上会在编译器自动替换。
- 由于需要使用JDK7编译，旧版本的ADT插件仅支持到1.6，因此你需要使用最新的ADT版本（23.0及以上）才能正确编译demo。

>由于google已经将Android Studio作为官方开发工具，不再提供ADT Bundle下载，继续使用Eclipse开发会让你错过很多新特性，因此我们强烈建议你使用Android Studio开发基于网易云信SDK的app，该demo的很多自动化特性也将只有Android Studio才能体验到。

## 源码结构

- Application入口：NimApplication, 包含SDK的初始化以及配置示例。
- 登录相关：login包，包含一个比较典型的从第三方app授权，然后登录到网易云信服务器的例子。
- 主界面：main包，包含最近联系人列表和好友/群组列表。该包作为各个功能的入口点，内含获取和管理最近联系人，获取群组列表，收发自定义通知等SDK接口使用示例。
- 消息相关：session包，包含单聊/群聊界面。内含收发消息，上传下载消息附件，使用高清语音，管理消息历史等SDK接口使用示例。该包还有消息展示，使用emoji表情，发送图片等示例代码，可供开发者参考。
- 群组相关：team包，包含群组信息界面。内含展示群资料，修改群资料，展示群成员列表，管理群成员列表等SDK接口使用示例。
- 网络通话相关：avchat包，包含网络通话界面。内含网络通话功能的SDK接口使用示例，以及一个完整的网络通话流程示例，开发者可直接参考开发网络通话功能。


## 修改demo为己用

网易云信demo实现了一个IM软件的所有基础功能，开发者可直接以demo为基础开发自己的IM软件，也可以稍作修改，用于前期流程验证，SDK开发指导等功能。

- 如果你已经在网易云信官网上注册了app，你需要修改AndroidManifest中的“com.netease.nim.appKey”为你自己的appkey，否则登录会失败。
- 你需要修改demo的好友获取方式。网易云信demo获取好友的代码在.contact.protocol包下，而用户资料缓存你可以直接重用。
