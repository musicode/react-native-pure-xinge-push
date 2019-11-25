package com.github.musicode.xingepush

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.tencent.android.tpush.XGIOperateCallback
import com.tencent.android.tpush.XGPushBaseReceiver
import com.tencent.android.tpush.XGPushConfig
import com.tencent.android.tpush.XGPushManager

import org.json.JSONException
import org.json.JSONObject

import androidx.collection.ArraySet
import me.leolin.shortcutbadger.ShortcutBadger

class RNTXingePushModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener, LifecycleEventListener {

    companion object {
        var isStarted = false
        var launchIntent: Intent? = null
    }

    private var badge = 0

    private var launchInfo: WritableMap? = null

    init {
        reactContext.addActivityEventListener(this)
        reactContext.addLifecycleEventListener(this)
        registerReceivers()
    }

    override fun getName(): String {
        return "RNTXingePush"
    }

    @ReactMethod
    fun setDebug(debug: Boolean) {
        XGPushConfig.enableDebug(reactContext, debug)
    }

    @ReactMethod
    fun enableOtherPush(enable: Boolean) {
        XGPushConfig.enableOtherPush(reactContext, enable)
    }

    @ReactMethod
    fun setHuaweiDebug(debug: Boolean) {
        XGPushConfig.setHuaweiDebug(debug)
    }

    @ReactMethod
    fun setXiaomi(appId: String, appKey: String) {
        XGPushConfig.setMiPushAppId(reactContext, appId)
        XGPushConfig.setMiPushAppKey(reactContext, appKey)
    }

    @ReactMethod
    fun setMeizu(appId: String, appKey: String) {
        XGPushConfig.setMzPushAppId(reactContext, appId)
        XGPushConfig.setMzPushAppKey(reactContext, appKey)
    }

    @ReactMethod
    fun start(accessId: Int, accessKey: String) {

        isStarted = true

        XGPushConfig.setAccessId(reactContext, accessId.toLong())
        XGPushConfig.setAccessKey(reactContext, accessKey)

        XGPushManager.registerPush(reactContext, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, flag: Int) {

                onStart(XGPushBaseReceiver.SUCCESS)
                onRegister(XGPushConfig.getToken(reactContext), XGPushBaseReceiver.SUCCESS)

            }

            override fun onFail(data: Any?, errCode: Int, msg: String) {
                onStart(errCode)
            }
        })

        if (launchInfo != null) {
            sendEvent("notification", launchInfo!!)
        }
        else if (launchIntent != null) {
            onNotifaction(launchIntent!!)
        }

        launchInfo = null
        launchIntent = null

    }

    @ReactMethod
    fun stop() {
        XGPushManager.unregisterPush(reactContext, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, flag: Int) {
                onStop(XGPushBaseReceiver.SUCCESS)
            }

            override fun onFail(data: Any?, errCode: Int, msg: String) {
                onStop(errCode)
            }
        })
    }

    @ReactMethod
    fun bindAccount(account: String) {
        XGPushManager.appendAccount(reactContext, account, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, flag: Int) {
                onBindAccount(XGPushBaseReceiver.SUCCESS)
            }

            override fun onFail(data: Any?, errCode: Int, msg: String) {
                onBindAccount(errCode)
            }
        })
    }

    @ReactMethod
    fun unbindAccount(account: String) {
        XGPushManager.delAccount(reactContext, account, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, flag: Int) {
                onUnbindAccount(XGPushBaseReceiver.SUCCESS)
            }

            override fun onFail(data: Any?, errCode: Int, msg: String) {
                onUnbindAccount(errCode)
            }
        })
    }

    @ReactMethod
    fun bindTags(tags: ReadableArray) {
        val set = ArraySet<String>()
        for (i in 0 until tags.size()) {
            set.add(tags.getString(i))
        }
        XGPushManager.addTags(reactContext, "addTags", set)
    }

    @ReactMethod
    fun unbindTags(tags: ReadableArray) {
        val set = ArraySet<String>()
        for (i in 0 until tags.size()) {
            set.add(tags.getString(i))
        }
        XGPushManager.deleteTags(reactContext, "deleteTags", set)
    }

    @ReactMethod
    fun setBadge(badge: Int) {
        this.badge = badge
        ShortcutBadger.applyCount(reactContext, badge)
    }

    @ReactMethod
    fun getBadge(promise: Promise) {
        val map = Arguments.createMap()
        map.putInt("badge", badge)
        promise.resolve(map)
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }

    private fun onStart(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("start", map)
    }

    private fun onStop(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("stop", map)
    }

    private fun onRegister(deviceToken: String, code: Int) {
        val map = Arguments.createMap()
        map.putString("deviceToken", deviceToken)
        map.putInt("error", code)
        sendEvent("register", map)
    }

    private fun onBindAccount(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("bindAccount", map)
    }

    private fun onUnbindAccount(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("unbindAccount", map)
    }

    private fun onBindTags(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("bindTags", map)
    }

    private fun onUnbindTags(code: Int) {
        val map = Arguments.createMap()
        map.putInt("error", code)
        sendEvent("unbindTags", map)
    }

    private fun onMessage(intent: Intent) {

        val customContent = intent.getStringExtra("customContent")

        sendEvent("message", getCustomContent(customContent))

    }

    private fun onNotifaction(intent: Intent) {

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val customContent = intent.getStringExtra("customContent")
        val clicked = intent.getBooleanExtra("clicked", false)
        val deleted = intent.getBooleanExtra("deleted", false)
        val presented = intent.getBooleanExtra("presented", false)

        val map = Arguments.createMap()
        if (clicked) {
            map.putBoolean("clicked", true)
        }
        if (deleted) {
            map.putBoolean("deleted", true)
        }
        if (presented) {
            map.putBoolean("presented", true)
        }

        val body = Arguments.createMap()
        body.putString("title", title)
        body.putString("content", content)
        map.putMap("body", body)

        map.putMap("custom_content", getCustomContent(customContent))

        if (isStarted) {
            sendEvent("notification", map)
        } else {
            launchInfo = map
        }

    }

    private fun getCustomContent(customContent: String?): WritableMap {

        val body = Arguments.createMap()

        if (customContent == null || customContent.isEmpty()) {
            return body
        }

        try {
            val json = JSONObject(customContent)
            val iterator = json.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                // 貌似信鸽只支持字符串
                body.putString(key, json.getString(key))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return body

    }

    override fun onHostResume() {
        XGPushManager.onActivityStarted(currentActivity)
    }

    override fun onHostPause() {
        XGPushManager.onActivityStoped(currentActivity)
    }

    override fun onHostDestroy() {

    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent) {

    }

    override fun onNewIntent(intent: Intent) {
        currentActivity?.let {
            // 后台运行时点击通知会调用
            it.intent = intent
        }
    }

    private fun registerReceivers() {

        val intentFilter = IntentFilter()
        intentFilter.addAction(Constant.ACTION_BIND_TAGS)
        intentFilter.addAction(Constant.ACTION_UNBIND_TAGS)
        intentFilter.addAction(Constant.ACTION_MESSAGE)
        intentFilter.addAction(Constant.ACTION_NOTIFICATION)

        reactContext.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Constant.ACTION_BIND_TAGS -> onBindTags(intent.getIntExtra("code", -1))
                    Constant.ACTION_UNBIND_TAGS -> onUnbindTags(intent.getIntExtra("code", -1))
                    Constant.ACTION_MESSAGE -> onMessage(intent)
                    Constant.ACTION_NOTIFICATION -> onNotifaction(intent)
                    else -> {
                    }
                }

            }
        }, intentFilter)
    }

}
