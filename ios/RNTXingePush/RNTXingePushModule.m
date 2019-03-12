
#import "RNTXingePushModule.h"
#import <React/RCTUtils.h>

static NSString *XingePushEvent_Start = @"start";
static NSString *XingePushEvent_Stop = @"stop";
static NSString *XingePushEvent_Resgiter = @"register";

static NSString *XingePushEvent_BindAccount = @"bindAccount";
static NSString *XingePushEvent_BindTag = @"bindTag";
static NSString *XingePushEvent_UnbindAccount = @"unbindAccount";
static NSString *XingePushEvent_UnbindTag = @"unbindTag";

static NSString *XingePushEvent_Message = @"message";
static NSString *XingePushEvent_Notification = @"notification";

static NSString *XingePushEvent_RemoteNotification = @"XingePushEvent_RemoteNotification";

static NSDictionary *RNTXingePush_LaunchUserInfo = nil;

// 获取自定义键值对
static NSMutableDictionary* XingePush_GetCustomContent(NSDictionary *userInfo) {
    
    NSMutableDictionary *customContent = [[NSMutableDictionary alloc] init];
    
    NSEnumerator *enumerator = [userInfo keyEnumerator];
    id key;
    while ((key = [enumerator nextObject])) {
        if (![key isEqual: @"xg"] && ![key isEqual: @"aps"]) {
            customContent[key] = userInfo[key];
        }
    }
    
    return customContent;
};

// 获取推送消息
static NSMutableDictionary* XingePush_GetNotification(NSDictionary *userInfo) {

    NSDictionary *customContent = XingePush_GetCustomContent(userInfo);

    NSDictionary *alert = userInfo[@"aps"][@"alert"];
    
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    dict[@"custom_content"] = customContent;
    dict[@"body"] = @{
                      @"title": alert[@"title"] ?: @"",
                      @"subtitle": alert[@"subtitle"] ?: @"",
                      @"content": alert[@"body"] ?: @""
                      };

    return dict;
    
};

@implementation RNTXingePushModule

// 在主工程 AppDelegate.m 里调下面几个 did 开头的方法

// didFinishLaunchingWithOptions return YES 之前调用
+ (void)didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // 为了更好的了解每一条推送消息的运营效果，需要将用户对消息的行为上报
    [[XGPush defaultManager] reportXGNotificationInfo:launchOptions];
    // 点击推送启动 App
    if ([launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]) {
        RNTXingePush_LaunchUserInfo = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    }
    else {
        RNTXingePush_LaunchUserInfo = nil;
    }
}

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {

    [[XGPush defaultManager] reportXGNotificationInfo:userInfo];
    [[NSNotificationCenter defaultCenter] postNotificationName:XingePushEvent_RemoteNotification object:userInfo];
    
    completionHandler(UIBackgroundFetchResultNewData);
    
}

// 信鸽服务启动的回调
- (void)xgPushDidFinishStart:(BOOL)isSuccess error:(NSError *)error {
    [self sendEventWithName:XingePushEvent_Start body:@{
                                                        @"error": @(isSuccess ? 0 : error ? error.code : 0)
                                                              }];
}

// 信鸽服务停止的回调
- (void)xgPushDidFinishStop:(BOOL)isSuccess error:(NSError *)error {
    [self sendEventWithName:XingePushEvent_Stop body:@{
                                                              @"error": @(isSuccess ? 0 : error ? error.code : 0)
                                                              }];
}

// 启动信鸽服务成功后，会触发此回调
- (void)xgPushDidRegisteredDeviceToken:(NSString *)deviceToken error:(NSError *)error {
    NSString *token = deviceToken ?: @"";
    [self sendEventWithName:XingePushEvent_Resgiter body:@{
                                                           @"deviceToken": token,
                                                           @"error": @(token.length > 0 ? 0 : error ? error.code : 0)
                                                           }];
}

// 绑定帐号或标签的回调
- (void)xgPushDidBindWithIdentifier:(NSString *)identifier type:(XGPushTokenBindType)type error:(NSError *)error {
    NSString *name = type == XGPushTokenBindTypeAccount ? XingePushEvent_BindAccount : XingePushEvent_BindTag;
    [self sendEventWithName:name body:@{
                                        @"error": @(error ? error.code : 0)
                                        }];
}

// 解除绑定帐号或标签的回调
- (void)xgPushDidUnbindWithIdentifier:(NSString *)identifier type:(XGPushTokenBindType)type error:(NSError *)error {
    NSString *name = type == XGPushTokenBindTypeAccount ? XingePushEvent_UnbindAccount : XingePushEvent_UnbindTag;
    [self sendEventWithName:name body:@{
                                        @"error": @(error ? error.code : 0)
                                        }];
}

- (void)didReceiveRemoteNotification:(NSNotification *)notification {

    NSDictionary *userInfo = notification.object;
    NSDictionary *aps = userInfo[@"aps"];

    int contentAvailable = 0;
    if ([aps objectForKey:@"content-available"]) {
        contentAvailable = [[NSString stringWithFormat:@"%@", aps[@"content-available"]] intValue];
    }

    if (contentAvailable == 1) {
        // 静默消息
        [self sendEventWithName:XingePushEvent_Message body:XingePush_GetCustomContent(userInfo)];
    }
    else {
        // 推送消息
        NSMutableDictionary *dict = XingePush_GetNotification(userInfo);
        dict[@"presented"] = @YES;
        
        [self sendEventWithName:XingePushEvent_Notification body:dict];
    }

}

// iOS 10 新增 API
// iOS 10 会走新 API, iOS 10 以前会走到老 API
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0

// App 用户点击通知
// App 用户选择通知中的行为
// App 用户在通知中心清除消息
// 无论本地推送还是远程推送都会走这个回调
- (void)xgPushUserNotificationCenter:(UNUserNotificationCenter *)center
      didReceiveNotificationResponse:(UNNotificationResponse *)response
               withCompletionHandler:(void (^)(void))completionHandler __IOS_AVAILABLE(10.0) {

    UNNotification *notification = response.notification;
    NSDictionary *userInfo = notification.request.content.userInfo;

    [[XGPush defaultManager] reportXGNotificationResponse:response];
    
    NSMutableDictionary *dict = XingePush_GetNotification(userInfo);
    dict[@"clicked"] = @YES;
    [self sendEventWithName:XingePushEvent_Notification body:dict];

    completionHandler();
}

// App 在前台弹通知需要调用这个接口
- (void)xgPushUserNotificationCenter:(UNUserNotificationCenter *)center
             willPresentNotification:(UNNotification *)notification
               withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler __IOS_AVAILABLE(10.0) {

    NSDictionary *userInfo = notification.request.content.userInfo;
    
    [[XGPush defaultManager] reportXGNotificationInfo:userInfo];
    
    NSMutableDictionary *dict = XingePush_GetNotification(userInfo);
    dict[@"presented"] = @YES;
    [self sendEventWithName:XingePushEvent_Notification body:dict];

    completionHandler(UNNotificationPresentationOptionBadge | UNNotificationPresentationOptionSound | UNNotificationPresentationOptionAlert);
}

#endif


RCT_EXPORT_MODULE(RNTXingePush);

- (instancetype)init {
    if (self = [super init]) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didReceiveRemoteNotification:)
                                             name:XingePushEvent_RemoteNotification
                                             object:nil];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter]removeObserver:self];
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        XingePushEvent_Start,
        XingePushEvent_Stop,
        XingePushEvent_Resgiter,
        XingePushEvent_BindAccount,
        XingePushEvent_BindTag,
        XingePushEvent_UnbindAccount,
        XingePushEvent_UnbindTag,
        XingePushEvent_Message,
        XingePushEvent_Notification
        ];
}

RCT_EXPORT_METHOD(start:(NSInteger)appID appKey:(NSString *)appKey) {
    [[XGPush defaultManager]startXGWithAppID:(uint32_t)appID appKey:appKey delegate:self];
    [XGPushTokenManager defaultTokenManager].delegate = self;
    if (RNTXingePush_LaunchUserInfo != nil) {
        NSMutableDictionary *dict = XingePush_GetNotification(RNTXingePush_LaunchUserInfo);
        dict[@"clicked"] = @YES;
        [self sendEventWithName:XingePushEvent_Notification body:dict];
        RNTXingePush_LaunchUserInfo = nil;
    }
}

RCT_EXPORT_METHOD(stop) {
    [[XGPush defaultManager] stopXGNotification];
}

RCT_EXPORT_METHOD(bindAccount:(NSString *)account) {
    [[XGPushTokenManager defaultTokenManager] bindWithIdentifier:account type:XGPushTokenBindTypeAccount];
}

RCT_EXPORT_METHOD(unbindAccount:(NSString *)account) {
    [[XGPushTokenManager defaultTokenManager] unbindWithIdentifer:account type:XGPushTokenBindTypeAccount];
}

RCT_EXPORT_METHOD(bindTag:(NSString *)tag) {
    [[XGPushTokenManager defaultTokenManager] bindWithIdentifier:tag type:XGPushTokenBindTypeTag];
}

RCT_EXPORT_METHOD(unbindTag:(NSString *)tag) {
    [[XGPushTokenManager defaultTokenManager] unbindWithIdentifer:tag type:XGPushTokenBindTypeTag];
}

RCT_EXPORT_METHOD(setBadge:(NSInteger)badge) {
    // 这里本地角标
    [[XGPush defaultManager] setXgApplicationBadgeNumber:badge];
    // 上报服务器，方便实现 +1 操作
    [[XGPush defaultManager] setBadge:badge];
}

RCT_EXPORT_METHOD(getBadge:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    NSInteger badge = [[XGPush defaultManager] xgApplicationBadgeNumber];
    resolve(@{
              @"badge": @(badge)
              });
}

RCT_EXPORT_METHOD(setDebug:(BOOL)enable) {
    [[XGPush defaultManager] setEnableDebug:enable];
}

@end
