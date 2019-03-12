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
