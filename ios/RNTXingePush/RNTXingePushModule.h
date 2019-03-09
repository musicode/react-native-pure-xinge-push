
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>

#import "XGPush.h"

@interface RNTXingePushModule : RCTEventEmitter <RCTBridgeModule, XGPushDelegate, XGPushTokenManagerDelegate>

+ (void)didFinishLaunchingWithOptions:(NSDictionary *)launchOptions;

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler;

@end
