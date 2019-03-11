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

// 支持 ios9 及以下加上这段
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
            XG_ACCESS_ID: "",
            XG_ACCESS_KEY: "",
            HW_APPID: "华为 appId"
        ]
    }
    // 线上包
    release {
        // 这里一般有一些别的配置

        // 重点是这三个配置项
        manifestPlaceholders = [
            XG_ACCESS_ID: "",
            XG_ACCESS_KEY: "",
            HW_APPID: "华为 appId"
        ]
    }
}
```

如果你的测试包或线上包开启了混淆，请在 `android/app/proguard-rules.pro` 加上这段：

```
# 如果您的项目中使用proguard等工具做了代码混淆，请保留以下选项，否则将导致信鸽服务不可用。
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep class com.tencent.android.tpush.** {* ;}
-keep class com.tencent.mid.** {* ;}
-keep class com.qq.taf.jce.** {*;}
-keep class com.tencent.bigdata.** {* ;}

# 华为通道
-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.android.hms.agent.**{*;}

# 小米通道
-keep class com.xiaomi.**{*;}
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver

# 魅族通道
-dontwarn com.meizu.cloud.pushsdk.**
-keep class com.meizu.cloud.pushsdk.**{*;}
```