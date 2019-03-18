# react-native-pure-xinge-push

信鸽 SDK 版本：

* ios: 3.3.5
* android: 4.3.2

## 安装

```
npm i react-native-pure-xinge-push
react-native link react-native-pure-xinge-push
```

## 配置

### iOS

`Build Phases` -> `Link Binary With Libraries` 添加下面几个库（先确定是否已添加...）：

* CoreTelephony.framework
* SystemConfiguration.framework
* UserNotifications.framework
* libz.tbd
* libsqlite3.0.tbd

`Capabilities` 打开推送

![](https://xg.qq.com/docs/assets/iOSXGCap.jpg)

修改项目 `AppDelegate.m`，如下：

```

#import <RNTXingePush/RNTXingePushModule.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

  // 这里有一堆固定的代码

  // 最后加上这句
  [RNTXingePushModule didFinishLaunchingWithOptions:launchOptions];

  return YES;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler {
  [RNTXingePushModule didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}

@end
```

### Android

`android/app/build.gradle` 加上这段

```
buildTypes {
    // 测试包
    debug {
        // 这里一般有一些别的配置

        // 重点是这三个配置项
        manifestPlaceholders = [
            XG_ACCESS_ID: "信鸽 accessId",
            XG_ACCESS_KEY: "信鸽 accessKey",
            HW_APPID: "华为 appId"
        ]
    }
    // 线上包
    release {
        // 这里一般有一些别的配置

        // 重点是这三个配置项
        manifestPlaceholders = [
            XG_ACCESS_ID: "信鸽 accessId",
            XG_ACCESS_KEY: "信鸽 accessKey",
            HW_APPID: "华为 appId"
        ]
    }
}
```

## 用法

```js
import XingePush from 'react-native-pure-xinge-push'

// 安卓开启厂商推送
XingePush.enableOtherPush(true)

// 配置华为，appId 写在 `android/app/build.gradle`，这里不用传了
// 这种脑残的方案也不知道是华为搞的还是信鸽搞的
XingePush.setHuaweiDebug(true)
// 配置小米 (string, string)
XingePush.setXiaomi(appId, appKey)
// 配置魅族 (string, string)
XingePush.setMeizu(appId, appKey)

// 安卓逻辑到此结束

// 配置信鸽 (number, string)
XingePush.start(xgAccessId, xgAccessKey)

// 监听事件
let result = XingePush.addEventListener('register', function (data) {

  // 信鸽错误码
  // ios: https://xg.qq.com/docs/ios_access/ios_returncode.html
  // android: https://xg.qq.com/docs/android_access/android_returncode.html
  if (data.error) {
    return
  }

  // 获取 token
  data.deviceToken

  // 绑定帐号 (string)
  XingePush.bindAccount('account')

  // 解除绑定帐号 (string)
  XingePush.unbindAccount('account')

  // 绑定标签 (Array)
  XingePush.bindTags(['tag1', 'tag2'])

  // 解除绑定标签 (Array)
  XingePush.unbindTags(['tag1', 'tag2'])
})
// 解绑事件
result.remove()

// 透传消息
XingePush.addEventListener('message', function (message) {
  // ios 通过静默消息实现
  // android 通过透传消息实现
  // 为了跨平台的兼容性，message 的数据全部来自 custom content
  // message 类型为对象
})

// 推送消息
XingePush.addEventListener('notification', function (notification) {

  // 推送是否弹出展现了
  notification.presented

  // 推送是否被用户点击了
  notification.clicked

  // 自定义的键值对
  notification.custom_content

  // 推送消息主体，安卓没法保证能取到正确的值，最好不要依赖这个字段
  notification.body
})
```

## 声明

保证会及时跟进最新版 SDK，放心使用。

